package cn.reve.framework.enums.file;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/27 20:18
 */
public enum FileStatusEnum {

    TRANSFER(0,"转码中"),
    TRANSFER_FAILED(1,"转码失败"),
    TANSFER_SUCCESS(2,"转码成功");
    private Integer status;
    private String description;

    private FileStatusEnum() {
    }

private FileStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }


    public String getDescription() {
        return description;
    }

}
