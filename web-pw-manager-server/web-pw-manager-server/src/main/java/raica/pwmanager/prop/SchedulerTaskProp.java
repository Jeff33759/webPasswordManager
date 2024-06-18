package raica.pwmanager.prop;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 管理排程任務的一些參數。
 */
@Configuration
public class SchedulerTaskProp {

    /**
     * 定期清理未進行激活的用戶。
     */
    @Getter
    @Value("${system-items.scheduler.task.inactivated-user-deletion.fixed-delay}")
    private String inactivatedUserDeletionTaskFixedDelay;

}
