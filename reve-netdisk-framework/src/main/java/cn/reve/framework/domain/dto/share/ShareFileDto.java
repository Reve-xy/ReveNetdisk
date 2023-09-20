package cn.reve.framework.domain.dto.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/4 14:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareFileDto {
    private String shareId;

    private String[] fileIds;
    private String fileName;

    private Integer expireType;
    /**
     * 是否自定义密码
     */
    private Boolean isDiy;
    private String code;
}
