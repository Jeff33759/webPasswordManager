package raica.pwmanager.dao.extension.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import raica.pwmanager.dao.base.CategoryMapper;
import raica.pwmanager.dao.extension.ICategoryService;
import raica.pwmanager.entities.po.Category;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
}
