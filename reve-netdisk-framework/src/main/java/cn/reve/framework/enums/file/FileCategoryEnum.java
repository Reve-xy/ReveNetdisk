package cn.reve.framework.enums.file;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/26 22:56
 */
public enum FileCategoryEnum {

    //分类类型
    ALL(0,"all","所有文件"),
    DOC(1,"doc","文档"),
    VIDEO(2,"video","视频"),
    MUSIC(3,"music","音频"),
    IMAGE(4,"image","图片" ),
    OTHERS(5,"others","其他");
    private Integer categoryId;
    private String category;
    private String cnName;

    FileCategoryEnum(Integer categoryId, String category, String cnName) {
        this.categoryId = categoryId;
        this.category = category;
        this.cnName = cnName;
    }

    public static Integer getIdByCategory(String category){
        for (FileCategoryEnum fileCategory : FileCategoryEnum.values()) {
            if (fileCategory.getCategory().equals(category)) {
                return fileCategory.getCategoryId();
            }
        }
        return null;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategory() {
        return category;
    }

    public String getCnName() {
        return cnName;
    }
}
