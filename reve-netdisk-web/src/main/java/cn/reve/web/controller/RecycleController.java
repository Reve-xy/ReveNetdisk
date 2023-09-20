package cn.reve.web.controller;

import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.file.QueryRecycleFileDto;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.service.FileService;
import cn.reve.framework.service.RecycleService;
import cn.reve.framework.utils.Result;
import cn.reve.framework.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/7/10 9:59
 */
@RestController
@RequestMapping("/recycle")
public class RecycleController {

    @Resource
    FileService fileService;

    @Resource
    RecycleService recycleService;


    @GetMapping("/loadRecycleList")
    @SystemLog("获取回收站文件")
    public Result<?> loadRecycleList(QueryRecycleFileDto queryRecycleFileDto) {
        return recycleService.loadRecycleList(queryRecycleFileDto);
    }

    @PostMapping("/revertFile")
    @SystemLog("恢复文件")
    public Result<?> recoverFile(@RequestBody List<String> fids) {
        return recycleService.revertFile(fids);
    }

    @DeleteMapping("/deleteRecycleFile")
    @SystemLog("彻底删除文件")
    public Result<?> deleteRecycleFile(@RequestBody List<String> fids) {
        // TODO: 2023/7/10  先做成把标志位改为-1的，后续再考虑要不要物理删除
        List<FileInfo> list = fileService.list(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getUserId, SecurityUtils.getUserId())
                .eq(FileInfo::getDelFlag, SystemConstants.RECYCLE_FLAG).in(FileInfo::getFileId, fids));
        list.stream().forEach(m->m.setDelFlag(SystemConstants.DEL_FLAG));
        fileService.updateBatchById(list);
        return Result.success();
    }
}
