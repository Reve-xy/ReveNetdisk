package cn.reve.framework.mapper;

import cn.reve.framework.domain.entity.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 文件表 Mapper 接口
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
public interface FileMapper extends BaseMapper<FileInfo> {

    List<FileInfo> getFolderNavigation(@Param("userId") String userId, @Param("folderType") Integer folderType,@Param("array") String[] pathArray);
}
