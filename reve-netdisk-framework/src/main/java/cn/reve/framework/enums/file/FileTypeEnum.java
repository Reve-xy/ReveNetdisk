package cn.reve.framework.enums.file;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/29 20:42
 */

public enum FileTypeEnum {
    /**
     * 0：目录 1：视频 2：音频 3：图片 4：PDF 5：Excel 6：Word 7：PPT 8：txt 9：code 10：可执行文件 11：zip 12：其他
     */
    VIDEO(FileCategoryEnum.VIDEO, 1, new String[]{".mp4", ".3gp", ".mkv", ".flv", ".wmv", ".mov", ".asf", ".qt",
            ".m4v", ".mov", ".vob", ".qt"}, "视频"),

    MUSIC(FileCategoryEnum.MUSIC,2,new String[] {
        ".mp3", ".wav", ".aiff", ".wma", "mp2", ".aac", ".m4a", ".mid", ".m4p", ".ape", ".m3u",
                ".xm", ".snd", ".mod", ".it", ".x", ".aac"
    },"音频"),

    IMAGE(FileCategoryEnum.IMAGE,3,new String[]{".jpg", ".jpeg", ".png", ".gif", ".tga", ".bmp", ".ico", ".psd",
            ".pspimage", ".tiff", ".3gpimage", ".pjpeg", ".pjp", ".svg", ".eps",},"图片"),

    PDF(FileCategoryEnum.DOC, 4,new String[]{".pdf"},"PDF"),
    EXCEL(FileCategoryEnum.DOC , 5,new String[]{".xls", ".xlsx",".xlt",".xltm",".xltx",".xlsm",".xltv",".xltv",".xlr"},"Excel"),
    WORD(FileCategoryEnum.DOC, 6,new String[]{".doc", ".docx",".dot",".dotm",".dotx"}, "Word"),
    PPT(FileCategoryEnum.DOC, 7,new String[]{".ppt", ".pptx",".pot",".potm"}, "PowerPoint"),
    TXT(FileCategoryEnum.DOC , 8,new String[]{".txt"},"文本"),
    PROGRAM(FileCategoryEnum.OTHERS,9,new String[]{".java",".py",".c",".cpp",".h",".cxx",".hh",".dox",".ini",".log",".md",".css"
    ,".html",".js",".scss",".less",".sh",".class",".vue",".jsx",".sql",".json",".xml",".iml",".c++"},"程序"),
    EXEC_FILE(FileCategoryEnum.OTHERS,10,new String[]{".exe",".lnk",".app",".msi",".apk",".bak",".sh"},"执行文件"),
    ZIP(FileCategoryEnum.OTHERS,11,new String[]{".zip",".rar",".7z",".xar",".cab",".gz",".iso",".dmg",".pkg"},"压缩包"),
    OTHER(FileCategoryEnum.OTHERS,12,new String[]{},"其他");

    private FileCategoryEnum fileCategoryEnum;
    private Integer typeId;
    private String[] suffix;
    private String description;

    public static FileTypeEnum getFileTypeEnumBySuffix(String fileSuffix){
        FileTypeEnum[] values = FileTypeEnum.values();
        List<FileTypeEnum> collect = Arrays.stream(values).filter(m -> Arrays.asList(m.getSuffix()).contains(fileSuffix)).collect(Collectors.toList());
        if(Objects.isNull(collect)){
            return FileTypeEnum.OTHER;
        }
        return collect.get(0);
    }

    FileTypeEnum(FileCategoryEnum fileCategoryEnum, Integer typeId, String[] suffix, String description) {
        this.fileCategoryEnum = fileCategoryEnum;
        this.typeId = typeId;
        this.suffix = suffix;
        this.description = description;
    }

    public FileCategoryEnum getFileCategoryEnum() {
        return fileCategoryEnum;
    }

    public void setFileCategoryEnum(FileCategoryEnum fileCategoryEnum) {
        this.fileCategoryEnum = fileCategoryEnum;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String[] getSuffix() {
        return suffix;
    }

    public void setSuffix(String[] suffix) {
        this.suffix = suffix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
