package raica.pwmanager.dao.extension.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import raica.pwmanager.dao.base.TagMappingPasswordMapper;
import raica.pwmanager.dao.extension.ITagMappingPassword;
import raica.pwmanager.entities.po.TagMappingPassword;

@Service
public class TagMappingPasswordService extends ServiceImpl<TagMappingPasswordMapper, TagMappingPassword> implements ITagMappingPassword {
}
