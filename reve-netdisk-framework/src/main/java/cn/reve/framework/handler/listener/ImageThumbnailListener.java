package cn.reve.framework.handler.listener;

import cn.reve.framework.constants.MqConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.file.ThumbnailMqDTO;
import cn.reve.framework.enums.file.FileTypeEnum;
import cn.reve.framework.service.FileService;
import cn.reve.framework.utils.FileUtils;
import cn.reve.framework.utils.MinioUtils;
import cn.reve.framework.utils.RandomCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/7/7 16:21
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = MqConstants.IMG_THUMB_TOPIC,
        consumerGroup = MqConstants.IMG_THUMB_GROUP)
public class ImageThumbnailListener implements RocketMQListener<ThumbnailMqDTO> {

    @Resource
    MinioUtils minioUtils;
    @Resource
    FileService fileService;

    String PNG_SUFFIX=".png";

    @Override
    public void onMessage(ThumbnailMqDTO thumbnailMqDTO) {
        InputStream inputStreamFromMinio;
        try {
            Integer fileType = thumbnailMqDTO.getFileType();
            //获取上传文件
            inputStreamFromMinio = minioUtils.getInputStreamFromMinio(thumbnailMqDTO.getFilePath());
            //临时文件
            File tempFile =new File(ResourceUtils.getURL("classpath:").getPath()+
                    RandomCodeGenerator.generateRandomCode(SystemConstants.FILE_ID_LENGTH)+PNG_SUFFIX);

            if(FileTypeEnum.IMAGE.getTypeId().equals(fileType)){
                Thumbnails.of(inputStreamFromMinio).scale(SystemConstants.SCALE_VAL).outputQuality(SystemConstants.QUALITY_VAL).toFile(tempFile);
            }else if(FileTypeEnum.VIDEO.getTypeId().equals(fileType)){
                FileUtils.getVideoThumbNail(inputStreamFromMinio,tempFile);
            }

            //转存到服务器
            InputStream thumbnailInputStream= Files.newInputStream(tempFile.toPath());
            String filePath = FileUtils.generateFilePath(thumbnailMqDTO.getUserId(),PNG_SUFFIX);
            minioUtils.uploadFile(thumbnailInputStream, filePath);
            String url=minioUtils.getFileUrl(filePath);
            //持久化到数据库
            fileService.setCover(thumbnailMqDTO.getFileId(),url);
            tempFile.delete();
            log.info("缩略图保存成功，地址为--->{}",url);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取文件流失败，无法创建缩略图--->{}", thumbnailMqDTO.getFileName());
        }
    }


}
