package cn.reve.framework.domain.dto.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/5 11:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareRedisValidDto {
    private LocalDateTime expireTime;
    private String shareId;
    private String userId;
    private String fileId;
}
