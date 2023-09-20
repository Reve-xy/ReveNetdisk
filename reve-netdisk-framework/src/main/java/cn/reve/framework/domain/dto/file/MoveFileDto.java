package cn.reve.framework.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/6/2 11:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveFileDto {
    private String[] fileIds;
    @NotBlank(message = "暂未选择移动到的目标文件夹")
    private String filePid;
}
