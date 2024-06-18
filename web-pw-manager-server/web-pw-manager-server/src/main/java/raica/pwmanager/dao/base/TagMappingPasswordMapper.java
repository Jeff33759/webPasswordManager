package raica.pwmanager.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import raica.pwmanager.entities.po.TagMappingPassword;

/**
 * @deprecated 存取DB統一用Service層，Mapper層棄用。
 */
@Mapper
public interface TagMappingPasswordMapper extends BaseMapper<TagMappingPassword> {

}
