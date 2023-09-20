package cn.reve.framework.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/6/23 11:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadingResVo {
    private String uploadId;
    private List<String> urlList;
}
