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
 * @date : 2023/5/27 14:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadStatusVo {
    private String uploadId;
    private String status;
    private List<Integer> chunkList;

    public UploadStatusVo(String uploadId, String status) {
        this.uploadId = uploadId;
        this.status = status;
    }
}
