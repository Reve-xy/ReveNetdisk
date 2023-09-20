package cn.reve.framework.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/6/2 8:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenameFileDto {
    private String fileName;
    private String fileId;
}
