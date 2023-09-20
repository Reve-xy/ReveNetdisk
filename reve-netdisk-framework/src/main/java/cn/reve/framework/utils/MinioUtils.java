package cn.reve.framework.utils;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.reve.framework.config.CustomMinioClient;
import cn.reve.framework.config.MinioConfig;
import cn.reve.framework.domain.vo.UploadingResVo;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import com.google.common.collect.HashMultimap;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListPartsResponse;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MinioUtils {
    
    @Resource
    MinioConfig minioConfig;
    @Resource
    CustomMinioClient client;

    private final String defaultContentType="application/octet-stream";

    /**
     * 单文件签名上传
     *
     * @param objectName 文件全路径名称
     * @return /
     */
    public Map<String, Object> getUploadObjectUrl(String objectName) {
        try {
            log.info("tip message: 通过 <{}-{}> 开始单文件上传<minio>", objectName, minioConfig.getBucketName());
            Map<String, Object> resMap = new HashMap();
            List<String> partList = new ArrayList<>();
            String url = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(minioConfig.getExpiry(), TimeUnit.DAYS)
                            .build());
            log.info("tip message: 单个文件上传、成功");
            partList.add(url);
            resMap.put("uploadId", "SingleFileUpload");
            resMap.put("urlList", partList);
            return resMap;
        } catch (Exception e) {
            log.error("error message: 单个文件上传失败、原因:", e);
            // 返回 文件上传失败
            return null;
        }
    }

    /**
     * 初始化分片上传
     *
     * @param uploadId
     * @param objectName     文件全路径名称
     * @param chunks      分片数量
     * @return Mono<Map < String, Object>>
     */
    public UploadingResVo initMultiPartUpload(String uploadId, String objectName, Integer chunkIndex,int chunks) {
        log.info("tip message: 通过 <{}-{}--{}> 开始初始化<分片上传>数据", objectName, chunks, minioConfig.getBucketName());
        try {

            //不设置content-type将无法预览
            HashMultimap<String, String> headers = HashMultimap.create();
            headers.put("Content-Type", getMimeType(objectName));

            //获取uploadId
            if(!StringUtils.hasText(uploadId)){
                uploadId = client.initMultiPartUpload(minioConfig.getBucketName(), null, objectName, headers, null);
            }
            UploadingResVo uploadingResVo = new UploadingResVo();
            uploadingResVo.setUploadId(uploadId);

            List<String> partUrlList = new ArrayList<>();
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("uploadId", uploadId);
            for (int i = chunkIndex; i <= chunks; i++) {
                reqParams.put("partNumber", String.valueOf(i));
                String uploadUrl = client.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .expiry(1, TimeUnit.DAYS)
                                .extraQueryParams(reqParams)
                                .build());
                partUrlList.add(uploadUrl);
            }
            log.info("tip message: 文件初始化<分片上传>、成功");
            uploadingResVo.setUrlList(partUrlList);
            return uploadingResVo;
        } catch (Exception e) {
            log.error("error message: 初始化分片上传失败、原因:", e);
            // 返回 文件上传失败
            throw new SystemException(HttpCodeEnum.INIT_UPLOAD_ERR);
        }
    }

    /**
     * 分片上传完后合并
     *
     * @param uploadId   返回的uploadId
     * @param objectName 文件全路径名称
     * @return boolean
     */
    public boolean mergeMultipartUpload(String uploadId,String objectName) {
        try {
            log.info("tip message: 通过 <{}-{}-{}> 合并<分片上传>数据", objectName, uploadId, minioConfig.getBucketName());
            //目前仅做了最大1000分片
            Part[] parts = new Part[1000];
            // 查询上传后的分片数据
            ListPartsResponse partResult = client.listMultipart(minioConfig.getBucketName(), null, objectName, 1000, 0, uploadId, null, null);
            int partNumber = 1;
            for (Part part : partResult.result().partList()) {
                parts[partNumber - 1] = new Part(partNumber, part.etag());
                partNumber++;
            }
            // 合并分片
            client.mergeMultipartUpload(minioConfig.getBucketName(), null,objectName, uploadId, parts, null, null);
        } catch (Exception e) {
            log.error("error message: 合并失败、原因:", e);
            return false;
        }
        return true;
    }

    /**
     * 通过 sha256 获取上传中的分片信息
     *
     * @param objectName 文件全路径名称
     * @param uploadId   返回的uploadId
     * @return Mono<Map < String, Object>>
     */
    public List<Integer> getChunkByFileMD5(String objectName, String uploadId) {
        log.info("通过 <{}-{}-{}> 查询<minio>上传分片数据", objectName, uploadId, minioConfig.getBucketName());
        try {
            // 查询上传后的分片数据
            ListPartsResponse partResult = client.listMultipart(minioConfig.getBucketName(), null, objectName, 1000, 0, uploadId, null, null);
            return partResult.result().partList().stream().map(Part::partNumber).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("error message: 查询上传后的分片信息失败、原因:", e);
            return null;
        }
    }

    /**
     * 获取文件地址
     *
     * @param fileName   文件名
     * @return
     */
    public String getFileUrl(String fileName) {
        return StrUtil.format("{}/{}/{}", minioConfig.getEndpoint(), minioConfig.getBucketName(), fileName);//文件访问路径
    }

    /**
     * 创建一个桶
     *
     * @return
     */
 /*   public String createBucket(String bucketName) {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            //如果桶存在
            if (client.bucketExists(bucketExistsArgs)) {
                return bucketName;
            }
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
            client.makeBucket(makeBucketArgs);
            return bucketName;
        } catch (Exception e) {
            log.error("创建桶失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }*/

    /**
     * 读取配置文件
     *
     * @param fileType
     * @return
     * @throws IOException
     */
    private String getProperty(String fileType) throws IOException {
        Properties SysLocalPropObject = new Properties();
        //判断桶关系配置文件是否为空
        if (SysLocalPropObject.isEmpty()) {
            InputStream is = getClass().getResourceAsStream("/BucketRelation.properties");
            SysLocalPropObject.load(is);
            is.close();
        }
        return SysLocalPropObject.getProperty("bucket." + fileType);
    }

    /**
     * 单文件直传
     *
     * @param file 文件
     * @return Boolean
     */
    public void upload(MultipartFile file,String fileName) throws Exception{
        if (!StringUtils.hasText(fileName)) {
            throw new RuntimeException();
        }
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            client.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 查看文件地址,要求无过期时间，
      /*  GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder()
                .bucket(minioConfig.getBucketName()).object(fileName).method(Method.GET).build();
        String url = null;
        url = client.getPresignedObjectUrl(build);
    
        return url;*/
    }

    /**
     * 缩略图上传
     * @param inputStream
     * @param fileName
     * @throws Exception
     */
    public void uploadFile(InputStream inputStream, String fileName) throws Exception {
        if (!StringUtils.hasText(fileName)) {
            throw new RuntimeException();
        }
        // 开始上传
        PutObjectArgs args = PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName)
                .stream(inputStream,inputStream.available(),-1).contentType(getMimeType(fileName)).build();
        client.putObject(args);
        //不可采取，有七天过期时间
        /*GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder()
                .bucket(minioConfig.getBucketName()).object(fileName).method(Method.GET).build();
        String url = null;
        url = client.getPresignedObjectUrl(build);
        return url;*/
    }


    /**
     * @param fileName
     * @param response
     * @return void
     * @date 2023/7/5 16:58
     * @description 下载文件
     */
    public void downloadFile(String fileName,String filePath,
                             HttpServletResponse response) {

        InputStream inputStream   = null;
        OutputStream outputStream = null;

        try {
            outputStream = response.getOutputStream();
            // 获取文件对象
            inputStream =client.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(filePath).build());
            if(inputStream==null){
                WebUtils.renderString(response,"文件下载失败，文件不存在");
            }
            byte buf[] = new byte[1024];
            int length = 0;
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            // 输出文件
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            log.info("下载{}文件成功,文件地址为{}",fileName,filePath);
            inputStream.close();
        } catch (Throwable ex) {
           WebUtils.renderString(response,"下载出现异常");
        } finally {
            try {
                outputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }}catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取文件流
     * @param fileName
     * @return
     * @throws Exception
     */
    public InputStream getInputStreamFromMinio(String fileName) throws Exception {
        return client.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName).build());
    }

    /**
     * 获取文件的 MimeType
     * @param fileUrl
     * @return
     */
    public  String getMimeType(String fileUrl)
    {
        return HttpUtil.getMimeType(fileUrl,defaultContentType);
    }
}