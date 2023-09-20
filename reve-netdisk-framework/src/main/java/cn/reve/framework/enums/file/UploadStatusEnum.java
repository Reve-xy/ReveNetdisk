package cn.reve.framework.enums.file;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/27 19:43
 */
public enum UploadStatusEnum {

    UPLOAD_SECONDS("uploadSeconds","秒传"),
    UPLOADING("uploading","上传中"),
    UPLOAD_FINISHED("uploadFinished","上传完成"),
    UPLOAD_FAILED("uploadFailed","上传失败"),
    NOT_UPLOAD("notUpload","未上传");

    UploadStatusEnum() {
    }

    UploadStatusEnum(String status, String description) {
        this.status = status;
        this.description = description;
    }

    private String status;
    private String description;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
