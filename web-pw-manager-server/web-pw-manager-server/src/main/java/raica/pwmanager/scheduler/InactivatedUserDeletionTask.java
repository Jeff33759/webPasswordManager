package raica.pwmanager.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import raica.pwmanager.dao.extension.impl.UserService;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.prop.ExpirationProps;
import raica.pwmanager.prop.SchedulerTaskProp;
import raica.pwmanager.util.DateUtil;
import raica.pwmanager.util.LogUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * 用於定期清理未激活的用戶。
 */
@Slf4j
@Component
public class InactivatedUserDeletionTask {

    @Autowired
    private UserService userService;

    @Autowired
    private SchedulerTaskProp schedulerTaskProp;

    @Autowired
    private ExpirationProps expirationProps;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private DateUtil dateUtil;


    @Scheduled(fixedDelayString = "#{@schedulerTaskProp.getInactivatedUserDeletionTaskFixedDelay()}")
    void deleteExpiredInactiveUsers() {
        Instant userInactivatedDeadLine = Instant.now().minus(expirationProps.getUserInactivatedExpiration());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("u_id AS id", "email"); //當select時，MyBatis Plus預設用java反射去映射DB Column name，所以若PO的ID欄位命名和DB不同，就要用AS，否則getId會是null
        userQueryWrapper.eq("is_activated", false);
        userQueryWrapper.le("u_create_time", userInactivatedDeadLine);

        List<User> usersTobeDeleted = userService.list(userQueryWrapper);

        List<Integer> toBeDeletedUserIds = usersTobeDeleted.stream().map(User::getId).toList();
        List<String> toBeDeletedUserEmails = usersTobeDeleted.stream().map(User::getEmail).toList();

        userService.removeByIds(toBeDeletedUserIds);

        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForScheduler(),
                String.format(
                        "The \"deleteExpiredInactiveUsers\" task executed successfully. userInactivatedDeadLine: %s. countOfDeletion: %d. deletedEmailList: %s.",
                        dateUtil.convertInstantToTimeFormatStringByZoneId(userInactivatedDeadLine, ZoneId.systemDefault()),
                        usersTobeDeleted.size(),
                        toBeDeletedUserEmails
                )
        );
    }

}
