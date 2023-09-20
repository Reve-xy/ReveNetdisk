package cn.reve.framework.domain.dto.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/4 14:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryShareDto {
    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 页大小
     */
    private Integer pageSize;

}
