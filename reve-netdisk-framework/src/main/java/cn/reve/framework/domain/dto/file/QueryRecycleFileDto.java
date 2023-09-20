package cn.reve.framework.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/7/10 10:05
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class QueryRecycleFileDto {
    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 页大小
     */
    private Integer pageSize;
}
