package raica.pwmanager.dao.extension.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import raica.pwmanager.dao.base.TagMapper;
import raica.pwmanager.dao.extension.ITagService;
import raica.pwmanager.entities.po.Tag;

@Service
public class TagService extends ServiceImpl<TagMapper, Tag> implements ITagService {
}
