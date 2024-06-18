package raica.pwmanager.util;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 日期時間轉換有關的工具。
 */
@Component
public class DateUtil {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeFormatter isoDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


    /**
     * 將Instant物件，轉換成國際標準時間(UTC)的ISO格式字串，顯示到秒數小數點後三位。
     * <p>
     * 輸出範例格式:
     * 2021-01-11T11:30:53.000Z
     *
     * @param instant 表示某個瞬間的物件
     * @return ISO格式時間字串
     */
    public String convertInstantToUTCTimeZoneISOString(Instant instant) {
        return isoDateTimeFormatter.withZone(ZoneId.from(ZoneOffset.UTC)).format(instant);
    }

    /**
     * 將Instant物件，轉換成yyyy-MM-dd HH:mm:ss格式的字串(此字串非絕對時間，時區由呼叫者給予ZoneId來決定)。
     * <p>
     * 輸出範例格式:
     * yyyy-MM-dd HH:mm:ss
     *
     * @param instant 表示某個瞬間的物件
     * @param zoneIdToBeOutput  指定欲輸出的時區
     * @return yyyy-MM-dd HH:mm:ss
     */
    public String convertInstantToTimeFormatStringByZoneId(Instant instant, ZoneId zoneIdToBeOutput) {
        return dateTimeFormatter.withZone(zoneIdToBeOutput).format(instant);
    }

    /**
     * 將yyyy-MM-dd HH:mm:ss格式的字串(此字串非絕對時間，時區由呼叫者給予ZoneId來決定)，轉換成instant物件。
     *
     * @param timeString 日期格式範例: 2021-10-08 23:11:38
     * @param zoneIdOfInput     指定timeString的時區
     * @return instant物件
     */
    public Instant convertTimeFormatStringToInstantByZoneId(String timeString, ZoneId zoneIdOfInput) {
        LocalDateTime localDateTime = LocalDateTime.parse(timeString, dateTimeFormatter);
        return localDateTime.toInstant(OffsetDateTime.now(zoneIdOfInput).getOffset());
    }

}
