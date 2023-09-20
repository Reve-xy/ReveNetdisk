package cn.reve.framework.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/27 19:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSpaceVo {


    /**
     * 账号
     */
    private String userId;

    /**
     * 使用的空间，byte
     */
    private Long useSpace;

    /**
     * 总空间
     */
    private Long totalSpace;
}
