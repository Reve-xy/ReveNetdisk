/**
 * @author Rêve
 * @date 2023/8/24 16:44
 * @description
 */
/**
 * 0：目录 1：视频 2：音频 3：图片 4：PDF 5：Excel 6：Word 7：PPT 8：txt 9：code 10：可执行文件 11：zip 12：其他
 */
export default {
    video: [".mp4", ".3gp", ".mkv", ".flv", ".wmv", ".mov", ".asf", ".qt",
        ".m4v", ".mov", ".vob", ".qt"],
    music: [
        ".mp3", ".wav", ".aiff", ".wma", "mp2", ".aac", ".m4a", ".mid", ".m4p", ".ape", ".m3u",
        ".xm", ".snd", ".mod", ".it", ".x", ".aac"],
    image: [".jpg", ".jpeg", ".png", ".gif", ".tga", ".bmp", ".ico", ".psd",
        ".pspimage", ".tiff", ".3gpimage", ".pjpeg", ".pjp", ".svg", ".eps"],
    pdf: [".pdf"],
    excel: [".xls", ".xlsx", ".xlt", ".xltm", ".xltx", ".xlsm", ".xltv", ".xltv", ".xlr"],
    word: [".doc", ".docx", ".dot", ".dotm", ".dotx"],
    ppt: [".ppt", ".pptx", ".pot", ".potm"],
    txt: ['.txt'],
    program: [".java", ".py", ".c", ".cpp", ".h", ".cxx", ".hh", ".dox", ".ini", ".log", ".md", ".css"
        , ".html", ".js", ".scss", ".less", ".sh", ".class", ".vue", ".jsx", ".sql", ".json", ".xml", ".iml", ".c++"],
    exec: [".exe", ".lnk", ".app", ".msi", ".apk", ".bak", ".sh"],
    zip: [".zip", ".rar", ".7z", ".xar", ".cab", ".gz", ".iso", ".dmg", ".pkg"],
    // other: [],
}
