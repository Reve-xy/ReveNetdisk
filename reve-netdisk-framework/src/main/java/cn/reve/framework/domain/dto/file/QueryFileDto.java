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
 * @date : 2023/5/26 22:23
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class QueryFileDto {

    /**
     * 文件名，用于查询时使用
     */
    private String fileName;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * 父级id
     */
    private String filePid;

    /**
     * 分类名
     */
    private String category;

    /**
     * 排序的列
     */
    private String order;

    /**
     * 排序方式，1为降序，0为升序
     */
    private Integer desc;


    /**
     * 当前查询的目录
     */
    // private String dir;


}
