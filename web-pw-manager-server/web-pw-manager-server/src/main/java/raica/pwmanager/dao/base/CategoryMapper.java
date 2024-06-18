package raica.pwmanager.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import raica.pwmanager.entities.po.Category;

/**
 * @deprecated 存取DB統一用Service層，Mapper層棄用。
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}
