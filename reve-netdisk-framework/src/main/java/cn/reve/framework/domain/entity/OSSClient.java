package cn.reve.framework.domain.entity;

import com.qiniu.http.Client;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/28 19:44
 */
@Data
@Service
@AllArgsConstructor
@NoArgsConstructor
public class OSSClient {
    // 设置好账号的ACCESS_KEY和SECRET_KEY
    String accessKey="hqJXQybMKH4VllaufxdImXozxhWw342A9ilS0Ev-";
    String secretKey="HgbKmmyXL1VnybPfkXdRbkpP49ui3TgPAt0cv3la";
    // 要上传的空间
    String bucket="reve-netdisk";

    Auth auth;

    Client client;

    {
        auth = Auth.create(accessKey, secretKey);
        Configuration configuration = new Configuration();
        client = new Client(configuration);
    }
}
