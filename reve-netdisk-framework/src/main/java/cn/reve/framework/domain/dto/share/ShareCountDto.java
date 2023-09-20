package cn.reve.framework.domain.dto.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/4 16:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareCountDto {
    /**
     * 访问量
     */
    private Long viewCount;
    /**
     * 保存量
     */
    private Long saveCount;

    /**
     * 下载量
     */
    private Long downloadCount;
;
}
