package cn.reve;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 17:31
 */
@SpringBootApplication
@MapperScan("cn.reve.*.mapper")
public class ReveApplication {
    public static void main(String[] args) {
        /*

         * 指定使用的日志框架，否则将会告警

         * RocketMQLog:WARN No appenders could be found for logger (io.netty.util.internal.InternalThreadLocalMap).

         * RocketMQLog:WARN Please initialize the logger system properly.

         */
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        SpringApplication.run(ReveApplication.class,args);
    }
}

