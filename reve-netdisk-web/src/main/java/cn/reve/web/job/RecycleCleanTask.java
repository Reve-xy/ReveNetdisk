package cn.reve.web.job;

import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.service.FileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 简要描述
 * 定期清除超时的文件
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/8/30 11:36
 */
@Component
public class RecycleCleanTask {

    @Resource
    FileService fileService;

    public void recycleClean(){
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getDelFlag, SystemConstants.RECYCLE_FLAG);
        wrapper.lt(FileInfo::getExpireTime, LocalDateTime.now());
        List<FileInfo> list = fileService.list(wrapper);
        list.forEach(fileInfo -> fileInfo.setDelFlag(SystemConstants.DEL_FLAG));
        fileService.updateBatchById(list);
    }
}
