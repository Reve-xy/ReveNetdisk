package cn.reve.framework.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 简要描述
 * 渲染内容到前端
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/3/31 15:57
 */
public class WebUtils {

    public static void renderString(HttpServletResponse response, String message){
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param filename
     * @param response
     * @return void
     * @date 2023/4/16 10:13
     * @description 设置下载头，下载excel
     */
    public static void setDownLoadHeader(String filename, HttpServletResponse response) throws UnsupportedEncodingException {
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        String fname= URLEncoder.encode(filename,"UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition","attachment; filename="+fname);
    }
}
