package cn.reve.web.controller;

import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.domain.dto.share.QueryShareDto;
import cn.reve.framework.domain.dto.share.ShareFileDto;
import cn.reve.framework.service.ShareService;
import cn.reve.framework.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 简要描述
 * 主页内分页列表的管理
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 15:18
 */
@RestController
@RequestMapping("/share")
public class ShareController {
    @Resource
    private ShareService shareService;

    @GetMapping("/listShareFile")
    @SystemLog("获取分享文件列表")
    public Result<?> listShareFile(QueryShareDto queryShareDto){
        return shareService.listShareFile(queryShareDto);
    }

    @PostMapping("/shareFile")
    @SystemLog("分享文件")
    public Result<?> shareFile(@RequestBody ShareFileDto shareFileDto){
        return shareService.saveShareFile(shareFileDto);
    }

    @DeleteMapping("/delShareFile")
    @SystemLog("取消分享")
    public Result<?> delShareFile(@RequestBody List<String> shareIds){
        return shareService.delShareFile(shareIds);
    }

}
