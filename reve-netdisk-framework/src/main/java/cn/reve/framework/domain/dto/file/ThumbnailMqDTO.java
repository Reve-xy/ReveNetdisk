package cn.reve.framework.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/7/8 19:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThumbnailMqDTO {
    private String userId;
    private String fileId;
    private Integer fileType;
    private String filePath;
    private String fileName;
}
