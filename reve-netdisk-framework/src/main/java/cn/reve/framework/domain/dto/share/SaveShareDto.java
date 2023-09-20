package cn.reve.framework.domain.dto.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/5 10:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveShareDto {
    String shareId;
    String[] shareFileIds;
    String myFolderId;
    String shareToken;
}
