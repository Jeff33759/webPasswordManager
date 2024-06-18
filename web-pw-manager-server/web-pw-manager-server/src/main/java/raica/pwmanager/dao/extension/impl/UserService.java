package raica.pwmanager.dao.extension.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import raica.pwmanager.dao.base.UserMapper;
import raica.pwmanager.dao.extension.IUserService;
import raica.pwmanager.entities.po.User;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {
}
