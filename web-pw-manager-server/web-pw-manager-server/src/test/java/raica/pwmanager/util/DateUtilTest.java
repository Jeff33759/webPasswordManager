package raica.pwmanager.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.ZoneId;

@SpringBootTest
class DateUtilTest {

    @Spy
    private DateUtil spyDateUtil; //待測元件


    @Test
    void GivenInstant_WhenConvertInstantToUTCTimeZoneISOString_ThenReturnExpectedString() {
        Instant inputInstant = Instant.ofEpochMilli(1652457600000L);
        String expectedTimeString = "2022-05-13T16:00:00.000Z";

        String actual = spyDateUtil.convertInstantToUTCTimeZoneISOString(inputInstant);

        Assertions.assertEquals(expectedTimeString, actual);
    }

    @Test
    void GivenInstant_WhenConvertInstantToTimeFormatStringByZoneId_ThenReturnExpectedString() {
        Instant inputInstant = Instant.ofEpochMilli(1652457600000L);
        ZoneId inputZoneId = ZoneId.of("Asia/Taipei");
        String expectedTimeString = "2022-05-14 00:00:00"; // UTC+8

        String actual = spyDateUtil.convertInstantToTimeFormatStringByZoneId(inputInstant, inputZoneId);

        Assertions.assertEquals(expectedTimeString, actual);
    }

    @Test
    void GivenTimeFormatStringAndZoneId_WhenConvertTimeFormatStringToInstantByZoneId_ThenReturnExpectedInstant() {
        String inputTimeString = "2022-05-14 00:00:00"; // UTC+8
        ZoneId inputZoneId = ZoneId.of("Asia/Taipei");
        Instant expectedInstant = Instant.ofEpochMilli(1652457600000L);

        Instant actual = spyDateUtil.convertTimeFormatStringToInstantByZoneId(inputTimeString, inputZoneId);

        Assertions.assertEquals(expectedInstant, actual);
    }


}