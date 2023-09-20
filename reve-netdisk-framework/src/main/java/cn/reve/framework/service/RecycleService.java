package cn.reve.framework.service;

import cn.reve.framework.domain.dto.file.QueryRecycleFileDto;
import cn.reve.framework.utils.Result;

import java.util.List;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/8/29 21:00
 */
public interface RecycleService {
    Result<?> loadRecycleList(QueryRecycleFileDto queryRecycleFileDto);

    Result<?> revertFile(List<String> fids);
}
