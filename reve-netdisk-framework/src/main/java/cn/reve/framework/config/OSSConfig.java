package cn.reve.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/29 21:16
 */
@Data
@Component
@ConfigurationProperties("oss")
public class OSSConfig {
    private String cdn;
    private String videoCamera;
    private String imageCamera;
}
