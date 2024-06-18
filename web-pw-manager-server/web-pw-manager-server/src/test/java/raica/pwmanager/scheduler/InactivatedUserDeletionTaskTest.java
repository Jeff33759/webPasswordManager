package raica.pwmanager.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import raica.pwmanager.dao.extension.impl.UserService;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.prop.ExpirationProps;
import raica.pwmanager.util.DateUtil;
import raica.pwmanager.util.LogUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InactivatedUserDeletionTaskTest {

    @Mock
    private UserService mockUserService;

    @Mock
    private ExpirationProps mockExpirationProps;

    @Mock
    private LogUtil mockLogUtil;

    @Mock
    private DateUtil mockDateUtil;

    @InjectMocks
    @Spy
    private InactivatedUserDeletionTask spyInactivatedUserDeletionTask;

    @Test
    void Given_WhenDeleteExpiredInactiveUsers_ThenExecuteExpectedProcessAndLogInfoLevel() {
        Duration stubInactivatedExpiration = Duration.parse("PT3M");
        Instant mockCurrentTime = Instant.ofEpochMilli(1716780260066L); //2024-05-27 11:24:20 Asia/Taipei
        Instant stubInstantOfUserInactivatedDeadLine = Instant.ofEpochMilli(1716780080066L); //2024-05-27 11:21:20 Asia/Taipei
        List<User> stubUsersToBeDeleted = List.of(new User().setId(1).setEmail("stubEmail"));

        try (MockedStatic<Instant> instantMockStatic = Mockito.mockStatic(Instant.class)) {
            instantMockStatic.when(Instant::now).thenReturn(mockCurrentTime);
            Mockito.when(mockCurrentTime.minus(stubInactivatedExpiration)).thenAnswer(invocationOnMock -> stubInstantOfUserInactivatedDeadLine);
            Mockito.when(mockExpirationProps.getUserInactivatedExpiration()).thenReturn(stubInactivatedExpiration);
            Mockito.when(mockUserService.list(Mockito.any(QueryWrapper.class))).thenReturn(stubUsersToBeDeleted);
            Mockito.when(mockDateUtil.convertInstantToTimeFormatStringByZoneId(stubInstantOfUserInactivatedDeadLine, ZoneId.systemDefault())).thenReturn("stubUserInactivatedDeadLine");
            Mockito.when(mockLogUtil.composeLogPrefixForScheduler()).thenReturn("[stubLogPrefix]");

            spyInactivatedUserDeletionTask.deleteExpiredInactiveUsers();
        }

        // 以下驗證，因為功能是跟delete有關，所以特別針對QueryWrapper的WHERE去驗證，以免之後有人改到CODE，把全部用戶都刪了
        ArgumentCaptor<QueryWrapper<User>> userQueryWrapperArgumentCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        ArgumentCaptor<List<Integer>> toBeDeletedUserIdsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mockExpirationProps, Mockito.times(1)).getUserInactivatedExpiration();
        Mockito.verify(mockUserService, Mockito.times(1)).list(userQueryWrapperArgumentCaptor.capture());
        Mockito.verify(mockUserService, Mockito.times(1)).removeByIds(toBeDeletedUserIdsArgumentCaptor.capture());
        Mockito.verify(mockLogUtil, Mockito.times(1)).logInfo(Mockito.any(), Mockito.eq("[stubLogPrefix]"), Mockito.eq("The \"deleteExpiredInactiveUsers\" task executed successfully. userInactivatedDeadLine: stubUserInactivatedDeadLine. countOfDeletion: 1. deletedEmailList: [stubEmail]."));
        //驗證wrapper是否設置正確
        QueryWrapper<User> targetWrapperToGetUsersToBeDeleted = userQueryWrapperArgumentCaptor.getValue();
        String selectCols = targetWrapperToGetUsersToBeDeleted.getSqlSelect();
        String condition = targetWrapperToGetUsersToBeDeleted.getCustomSqlSegment();
        Assertions.assertEquals("u_id AS id,email", selectCols);
        Assertions.assertTrue(condition.contains("WHERE"));
        Assertions.assertTrue(condition.contains("is_activated ="));
        Assertions.assertTrue(condition.contains("u_create_time <="));
        //驗證removeByIds是否傳參正確
        List<Integer> targetListOfUsersToBeDeleted = toBeDeletedUserIdsArgumentCaptor.getValue();
        Assertions.assertEquals(1, targetListOfUsersToBeDeleted.size());
        Assertions.assertEquals(1, targetListOfUsersToBeDeleted.get(0));
    }

}