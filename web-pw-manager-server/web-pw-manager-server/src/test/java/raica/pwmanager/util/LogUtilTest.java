package raica.pwmanager.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import raica.pwmanager.enums.LogType;

import java.util.Optional;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class LogUtilTest {

    @Spy
    private LogUtil spyLogUtil; //待測元件


    @Test
    void GivenArgs_WhenComposeLogPrefixForBusiness_ThenReturnExpectedString() {
        Optional<Integer> inputUserIdOpt = Optional.of(1);
        String inputUUID = "stubUUID";
        String expectedLogContent = String.format("[%s][1][stubUUID]", LogType.BUSINESS.getTypeName());

        String actual = spyLogUtil.composeLogPrefixForBusiness(inputUserIdOpt, inputUUID);

        Assertions.assertEquals(expectedLogContent, actual);
    }

    @Test
    void GivenUserIdOptIsEmpty_WhenComposeLogPrefixForBusiness_ThenReturnExpectedString() {
        Optional<Integer> inputUserIdOpt = Optional.empty();
        String inputUUID = "stubUUID";
        String expectedLogContent = String.format("[%s][NULL][stubUUID]", LogType.BUSINESS.getTypeName());

        String actual = spyLogUtil.composeLogPrefixForBusiness(inputUserIdOpt, inputUUID);

        Assertions.assertEquals(expectedLogContent, actual);
    }

    @Test
    void GivenArgs_WhenComposeLogPrefixForSystem_ThenReturnExpectedString() {
        String expectedLogContent = String.format("[%s]", LogType.SYSTEM.getTypeName());

        String actual = spyLogUtil.composeLogPrefixForSystem();

        Assertions.assertEquals(expectedLogContent, actual);
    }

    @Test
    void GivenArgs_WhenComposeLogPrefixForScheduler_ThenReturnExpectedString() {
        String expectedLogContent = String.format("[%s]", LogType.SCHEDULER.getTypeName());

        String actual = spyLogUtil.composeLogPrefixForScheduler();

        Assertions.assertEquals(expectedLogContent, actual);
    }


    @Test
    void GivenArgs_WhenGenerateUUIDForLogging_ThenReturnExpectedLengthString() {
        int expectedLength = 6;

        String actual = spyLogUtil.generateUUIDForLogging();

        Assertions.assertEquals(expectedLength, actual.length());
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogInfo_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);
        String inputPrefix = "stubPrefix";
        String inputMsg = "stubMsg";

        spyLogUtil.logInfo(mockLogger, inputPrefix, inputMsg);

        Mockito.verify(mockLogger, Mockito.times(1)).info("{} {}", inputPrefix, inputMsg);
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogDebug_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);
        String inputPrefix = "stubPrefix";
        String inputMsg = "stubMsg";

        spyLogUtil.logDebug(mockLogger, inputPrefix, inputMsg);

        Mockito.verify(mockLogger, Mockito.times(1)).debug("{} {}", inputPrefix, inputMsg);
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogWarn_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);
        String inputPrefix = "stubPrefix";
        String inputMsg = "stubMsg";

        spyLogUtil.logWarn(mockLogger, inputPrefix, inputMsg);

        Mockito.verify(mockLogger, Mockito.times(1)).warn("{} {}", inputPrefix, inputMsg);
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogError_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);
        String inputPrefix = "stubPrefix";
        Exception inputException = new Exception("stubMsg");

        spyLogUtil.logError(mockLogger, inputPrefix, inputException.getMessage(), inputException);

        Mockito.verify(mockLogger, Mockito.times(1)).error("{} {}", inputPrefix, inputException.getMessage(), inputException);
    }

}