package cn.reve.web;

import cn.reve.framework.service.impl.LoginServiceImpl;
import cn.reve.framework.service.impl.UploadServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 17:32
 */
@SpringBootTest
public class testService {
    @Resource
    LoginServiceImpl loginService;

    @Test
    public void testSendEmail() {
        // loginService.sendEmail("2731592737@qq.com");
    }

    @Resource
    UploadServiceImpl uploadService;

    @Test
    public void testFile() {
        String fn = "sg.zip";
        File file = new File("D:\\下载路径\\Browser_download\\SGBlog (20).zip");
        String uploadId = uploadService.initiate(fn);
        long partOffset = 0; // 块在文件中的偏移量
        int partNumber = 1; // part num 从 1 开始
        long fileSize = file.length();
        int defaultPartSize = 1024 * 1024 * 2;
        RandomAccessFile file1 = null;
        try {
            file1 = new RandomAccessFile(file, "r");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (partOffset < fileSize) {
            // 1.2.1 读取 part 数据
            long partSize = fileSize - partOffset;
            if (partSize > defaultPartSize) {
                partSize = defaultPartSize;
            }
            byte[] partData = getUploadData(file1, partOffset, (int) partSize);
            try {
                uploadService.uploadPart(partData,fn,uploadId,partNumber);
                // 1.2.3 计算下一个 part 的 number
                partNumber += 1;
                partOffset += partSize;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        uploadService.completePart(uploadId,fn);
    }

    private byte[] getUploadData(RandomAccessFile file, long offset, int size) {
        byte[] uploadData = new byte[size];
        try {
            file.seek(offset);
            int readSize = 0;
            while (readSize != size) {
                int s = 0;
                try {
                    s = file.read(uploadData, readSize, size - readSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s >= 0) {
                    readSize += s;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            uploadData = null;
        }
        return uploadData;
    }
}