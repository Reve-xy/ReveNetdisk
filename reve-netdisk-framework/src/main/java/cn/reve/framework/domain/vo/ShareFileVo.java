package cn.reve.framework.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/5 23:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareFileVo {
    private String shareId;
    private String fileId;
    private Long expireType;
    private String code;
    private String userId;
}
