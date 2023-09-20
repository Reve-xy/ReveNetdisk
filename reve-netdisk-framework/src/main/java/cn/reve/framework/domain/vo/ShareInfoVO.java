package cn.reve.framework.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareInfoVO {
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private String nickName;
    private String fileName;
    private String fileId;
    private String avatar;
    private String qqAvatar;
    private String userId;
    private String signature;
}