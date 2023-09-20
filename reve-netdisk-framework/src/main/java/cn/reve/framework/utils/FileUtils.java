package cn.reve.framework.utils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/27 20:06
 */
public class FileUtils {

    public static final String NORMAL_FILE_FOLDER="Normal_File/";
    public static final String THUMBNAIL_FILE_FOLDER="Thumbnail_File/";


    private FileUtils() {
    }

    public static String getFilePrefix(String fileName) {
        Integer i = fileName.lastIndexOf(".");
        if (i == -1) {
            return fileName;
        }
        return fileName.substring(0, i);
    }

    public static String getFileSuffix(String fileName) {
        Integer i = fileName.lastIndexOf(".");
        if (i == -1) {
            return fileName;
        }
        return fileName.substring(i);
    }

    public static String renameFile(String fileName, int fileNum) {
        StringBuilder stringBuilder = new StringBuilder();
        String filePrefix = getFilePrefix(fileName);
        String fileSuffix = getFileSuffix(fileName);
        stringBuilder.append(filePrefix);
        stringBuilder.append("（");
        stringBuilder.append(fileNum);
        stringBuilder.append("）");
        stringBuilder.append(fileSuffix);
        return stringBuilder.toString();
    }


    /**
     * @param fileName
     * @return String
     * @date 2023/6/1 21:06
     * @description 目录重命名，xxx_date_time;
     */
    public static String renameFolder(String fileName){
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String format = LocalDateTime.now().format(formatters);
        String prefix=format.substring(0,6);
        String suffix=format.substring(6);
        return new StringBuilder().append(fileName).append("_").append(prefix).append("_").append(suffix).toString();
    }

    /**
     * @return String
     * @date 2023/6/1 20:56
     * @description 根据文件名生成oss中存储的文件及目录
     */
    public static String generateFilePath(String fileSuffix){

        //根据日期生成路径   2022/1/
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("/yyyy/MM/");
        String filePath;
        filePath = FileUtils.NORMAL_FILE_FOLDER+SecurityUtils.getUserId()+LocalDate.now().format(formatters);
        //uuid作为文件名
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        return new StringBuilder().append(filePath).append(uuid).append(fileSuffix).toString();
    }

    //生成缩略图需要传userID
    public static String generateFilePath(String userId,String fileSuffix){

        //根据日期生成路径   2022/1/
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("/yyyy/MM/");
        String filePath;
        filePath = FileUtils.THUMBNAIL_FILE_FOLDER+userId+LocalDate.now().format(formatters);
        //uuid作为文件名
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        return new StringBuilder().append(filePath).append(uuid).append(fileSuffix).toString();
    }


    /**
     * @param inputStream
     * @param tempFile
     * @return void
     * @date 2023/7/8 18:54
     * @description 将视频缩略图保存到tempFile
     */
   public static void getVideoThumbNail(InputStream inputStream, File tempFile){
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(inputStream);
        try {
            ff.start();
            int videoLength = ff.getLengthInFrames();
            Frame f;
            int i=0;
            while (i < videoLength) {
                f = ff.grabImage();
                //截取第6帧
                if ((i > 5) && (f.image != null)) {
                    doExecuteFrame(f,tempFile);
                    break;
                }
                i++;
            }
            ff.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 截取视频
     * @param f
     * @param tempFile
     */
    public static void doExecuteFrame(Frame f,File tempFile) {
        int owidth = f.imageWidth;
        int oheight = f.imageHeight;
        // 对截取的帧进行等比例缩放
        int width = 300;
        int height = (int) (((double) width / owidth) * oheight);

        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.getBufferedImage(f);
        try {
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            bi.getGraphics().drawImage(bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH),
                    0, 0, null);
            ImageIO.write(bi, "png", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

