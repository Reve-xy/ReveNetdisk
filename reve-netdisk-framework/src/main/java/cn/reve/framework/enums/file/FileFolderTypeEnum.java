package cn.reve.framework.enums.file;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/30 15:26
 */
public enum FileFolderTypeEnum {
    /**
     * 1--普通文件
     * 0--文件夹
     */

    FILE(1,"普通文件"),
    FOLDER(0,"文件夹");

    private Integer typeId;
    private String typeName;

    FileFolderTypeEnum(Integer typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
