package raica.pwmanager.dao.extension.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import raica.pwmanager.dao.base.PasswordMapper;
import raica.pwmanager.dao.extension.IPasswordService;
import raica.pwmanager.entities.po.Password;

@Service
public class PasswordService extends ServiceImpl<PasswordMapper, Password> implements IPasswordService {
}
