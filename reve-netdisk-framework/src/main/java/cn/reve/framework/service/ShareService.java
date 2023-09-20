package cn.reve.framework.service;

import cn.reve.framework.domain.dto.share.CheckShareCodeDto;
import cn.reve.framework.domain.dto.share.QueryShareDto;
import cn.reve.framework.domain.dto.share.ShareFileDto;
import cn.reve.framework.domain.entity.ShareInfo;
import cn.reve.framework.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 分享列表 服务类
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
public interface ShareService extends IService<ShareInfo> {

    Result<?> listShareFile(QueryShareDto queryShareDto);
    /**
     * @param shareFileDto
     * @return Result<?>
     * @date 2023/9/4 14:34
     * @description 保存分享记录
     */
    Result<?> saveShareFile(ShareFileDto shareFileDto);

    Result<?> delShareFile(List<String> shareIds);

    Result<?> checkShareCode(CheckShareCodeDto checkShareCodeDto);
    Long covertSecondToDay(Long validDate, LocalDateTime createTime, LocalDateTime now);
}
