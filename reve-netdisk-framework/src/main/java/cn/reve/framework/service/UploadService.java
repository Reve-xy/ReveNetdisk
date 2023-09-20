package cn.reve.framework.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/28 20:30
 */
public interface UploadService {
    String initiate(String fileName);

    String uploadPart(MultipartFile file, String fileName, String uploadId, Integer partNumber) throws IOException;

    void completePart(String uploadId, String fileName);

    void abortUpload(String uploadId, String fileName);

    String uploadImg(MultipartFile imgFile, String fileName);
}
