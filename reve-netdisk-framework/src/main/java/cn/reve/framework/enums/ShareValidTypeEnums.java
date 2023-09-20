package cn.reve.framework.enums;

public enum ShareValidTypeEnums {
    DAY_1(0, 1L, "1天"),
    DAY_7(1, 7L, "7天"),
    DAY_30(2, 30L, "30天"),
    FOREVER(3, -1L, "永久有效");

    private Integer type;
    private Long days;
    private String desc;

    ShareValidTypeEnums(Integer type, Long days, String desc) {
        this.type = type;
        this.days = days;
        this.desc = desc;
    }


    public static ShareValidTypeEnums getByType(Integer type) {
        for (ShareValidTypeEnums typeEnums : ShareValidTypeEnums.values()) {
            if (typeEnums.getType().equals(type)) {
                return typeEnums;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public Long getDays() {
        return days;
    }

    public String getDesc() {
        return desc;
    }
}
