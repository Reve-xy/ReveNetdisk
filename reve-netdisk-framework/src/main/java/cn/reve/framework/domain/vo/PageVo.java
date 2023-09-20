package cn.reve.framework.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简要描述
 * 封装分页查询结果
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/24 21:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVo<T> {
    private List<T> rows;
    private Long total;
}
