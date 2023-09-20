package cn.reve.framework.service.impl;

import com.google.gson.Gson;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.domain.entity.OSSClient;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.service.UploadService;
import cn.reve.framework.utils.RedisCache;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.DefaultPutRet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 简要描述
 * 初始化，上传分片，合并文件的文件名必须一致
 * 七牛云
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/28 16:30
 */
@Data
@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {

    @Resource
    OSSClient ossClient;

    /**
     * 要上传的空间对应的 urlPrefix scheme + host
     * host:
     * 华东机房(region0): up.qiniup.com 或 upload.qiniup.com
     * 华北机房(region1): up-z1.qiniup.com 或 upload-z1.qiniup.com
     * 华南机房(region2): up-z2.qiniup.com 或 upload-z2.qiniup.com
     * 北美机房(regionNa0): up-na0.qiniup.com 或 upload-na0.qiniup.com
     * 新加坡机房(regionAs0): up-as0.qiniup.com 或 upload-as0.qiniup.com
     * 雾存储华东一区(regionFogCnEast1): up-fog-cn-east-1.qiniup.com 或 upload-fog-cn-east-1.qiniup.com
     */
    @Value("${oss.urlPrefix}")
    String urlPrefix;


    String getUploadToken() {
        return ossClient.getAuth().uploadToken(ossClient.getBucket());
    }

    /**
     * 获取上传的id
     *
     * @param fileName
     * @return
     */
    @Override
    public String initiate(String fileName) {
        // 1. init upload
        String uploadId = null;
        ApiUploadV2InitUpload initUploadApi = new ApiUploadV2InitUpload(ossClient.getClient());
        ApiUploadV2InitUpload.Request initUploadRequest = new ApiUploadV2InitUpload.Request(urlPrefix, getUploadToken())
                .setKey(fileName);
        try {
            ApiUploadV2InitUpload.Response initUploadResponse = initUploadApi.request(initUploadRequest);
            uploadId = initUploadResponse.getUploadId();
            log.info("init upload:" + initUploadResponse.getResponse());
            log.info("init upload id::" + initUploadResponse.getUploadId());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return uploadId;
    }


    @Resource
    RedisCache redisCache;
    @Resource
    RedisTemplate redisTemplate;

    @Override
    public String uploadPart(MultipartFile file, String fileName, String uploadId, Integer partNumber) throws IOException {
        byte[] partData = file.getBytes();
        // 1.2.2 上传 part 数据
        ApiUploadV2UploadPart uploadPartApi = new ApiUploadV2UploadPart(ossClient.getClient());
        ApiUploadV2UploadPart.Request uploadPartRequest = new ApiUploadV2UploadPart.Request(urlPrefix, getUploadToken(), uploadId, partNumber)
                .setKey(fileName)
                .setUploadData(partData, 0, partData.length, null);
        try {
            ApiUploadV2UploadPart.Response uploadPartResponse = uploadPartApi.request(uploadPartRequest);
            //获取响应体内容
            String etag = uploadPartResponse.getEtag();
            String md5 = uploadPartResponse.getMd5();
            Map<String, Object> partInfo = new HashMap<>();
            partInfo.put(ApiUploadV2CompleteUpload.Request.PART_NUMBER, partNumber);
            partInfo.put(ApiUploadV2CompleteUpload.Request.PART_ETG, etag);
            //暂存合并文件所需的内容到redis
            redisTemplate.opsForList().rightPush(uploadId, partInfo);
            redisCache.setCacheObject(RedisKeyConstants.UPLOAD_CHUNK_KEY,partNumber);
            log.info("upload partNum:" +partNumber);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    public String uploadPart(byte[] file, String fileName, String uploadId, Integer partNumber) throws IOException {
        // 1.2.2 上传 part 数据
        ApiUploadV2UploadPart uploadPartApi = new ApiUploadV2UploadPart(ossClient.getClient());
        ApiUploadV2UploadPart.Request uploadPartRequest = new ApiUploadV2UploadPart.Request(urlPrefix, getUploadToken(), uploadId, partNumber)
                .setKey(fileName)
                .setUploadData(file, 0, file.length, null);
        try {
            ApiUploadV2UploadPart.Response uploadPartResponse = uploadPartApi.request(uploadPartRequest);
            //获取响应体内容
            String etag = uploadPartResponse.getEtag();
            String md5 = uploadPartResponse.getMd5();
            Map<String, Object> partInfo = new HashMap<>();
            partInfo.put(ApiUploadV2CompleteUpload.Request.PART_NUMBER, partNumber);
            partInfo.put(ApiUploadV2CompleteUpload.Request.PART_ETG, etag);
            //暂存合并文件所需的内容到redis
            redisTemplate.opsForList().rightPush(uploadId, partInfo);
            redisCache.setCacheObject(RedisKeyConstants.UPLOAD_CHUNK_KEY,partNumber);
            log.info("upload partNum:" +partNumber);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return "ok";
    }
    @Override
    public void completePart(String uploadId, String fileName) {
      /*  // 获取上传的 part 信息
        Integer partNumberMarker = null;
        List<Map<String, Object>> listPartInfo = new ArrayList<>();
        while (true) {
            ApiUploadV2ListParts listPartsApi = new ApiUploadV2ListParts(ossClient.getClient());
            ApiUploadV2ListParts.Request listPartsRequest = new ApiUploadV2ListParts.Request(urlPrefix, ossClient.getToken(), uploadId)
                    .setKey(fileName)
                    // .setMaxParts(2)  // 此处仅为示例分页拉去，实际可不配置使用默认值1000
                    .setPartNumberMarker(partNumberMarker);
            try {
                ApiUploadV2ListParts.Response listPartsResponse = listPartsApi.request(listPartsRequest);
                partNumberMarker = listPartsResponse.getPartNumberMarker();
                listPartInfo.addAll(listPartsResponse.getParts());
                System.out.println("list part:" + listPartsResponse.getResponse());
                // 列举结束
                if (partNumberMarker == 0) {
                    break;
                }
            } catch (QiniuException e) {
                e.printStackTrace();
                break;
            }
        }
        System.out.println("list parts info:" + listPartInfo);*/

        // 3. 组装文件
        ApiUploadV2CompleteUpload completeUploadApi = new ApiUploadV2CompleteUpload(ossClient.getClient());
        ApiUploadV2CompleteUpload.Request completeUploadRequest = new ApiUploadV2CompleteUpload.Request(urlPrefix, getUploadToken(), uploadId, redisCache.getCacheList(uploadId))
                .setKey(fileName)
                .setFileName(fileName);
        try {
            ApiUploadV2CompleteUpload.Response completeUploadResponse = completeUploadApi.request(completeUploadRequest);
           log.info("complete upload:" + completeUploadResponse.getResponse());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

    }

    /**
     * 取消上传
     * @param uploadId
     * @param fileName
     */
    @Override
    public void abortUpload(String uploadId,String fileName){
        ApiUploadV2AbortUpload apiUploadV2AbortUpload=new ApiUploadV2AbortUpload(ossClient.getClient());
        ApiUploadV2AbortUpload.Request abortRequest = new ApiUploadV2AbortUpload.Request(urlPrefix, getUploadToken(), uploadId).setKey(fileName);
        try {
            ApiUploadV2AbortUpload.Response abortResponse = apiUploadV2AbortUpload.request(abortRequest);
            log.info("abort upload:" + abortResponse.getResponse());
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param imgFile
     * @param fileName
     * @return String
     * @date 2023/5/29 14:21
     * @description 上传图片
     */
    @Override
    public String uploadImg(MultipartFile imgFile,String fileName){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.autoRegion());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名

        try {
            InputStream inputStream = imgFile.getInputStream();
            String upToken = getUploadToken();
            try {
                Response response = uploadManager.put(inputStream,fileName,upToken,null, null);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
                return "http://rvb4ht0v9.hn-bkt.clouddn.com"+fileName;
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                throw new SystemException(HttpCodeEnum.FAILURE_UPLOAD);
            }
        } catch (Exception ex) {
            //ignore
            throw new SystemException(HttpCodeEnum.FAILURE_UPLOAD);
        }
    }
}
