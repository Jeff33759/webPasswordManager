package raica.pwmanager.util;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import raica.pwmanager.enums.LogType;

import java.util.Optional;
import java.util.UUID;

/**
 * 統一管理Log格式。
 * [Log類型][請求者的UserId(若無則為NULL)][UUID] Log本體
 */
@Component
public class LogUtil {

    /**
     * 系統通知Logging的前綴。
     * 因為永遠不會變動，所以寫成常數，不用每次呼叫log方法時都要重新組成。
     */
    private final String LOG_PREFIX_FOR_SYSTEM = String.format("[%s]", LogType.SYSTEM.getTypeName());

    /**
     * 用於業務邏輯Logging前綴的一部份。
     * 這一部分因為是永遠不會變動的，所以寫成常數，不用每次呼叫log方法時都要重新組成這一段。
     */
    private final String PART_OF_LOG_PREFIX_FOR_BUSINESS = String.format("[%s]", LogType.BUSINESS.getTypeName());

    /**
     * 用於排程Logging的前綴。
     * 這一部分因為是永遠不會變動的，所以寫成常數，不用每次呼叫log方法時都要重新組成這一段。
     */
    private final String LOG_PREFIX_FOR_SCHEDULER = String.format("[%s]", LogType.SCHEDULER.getTypeName());


    public void logInfo(Logger logger, String logPrefix, String logMsg) {
        logger.info("{} {}", logPrefix, logMsg);
    }

    public void logDebug(Logger logger, String logPrefix, String logMsg) {
        logger.debug("{} {}", logPrefix, logMsg);
    }

    public void logWarn(Logger logger, String logPrefix, String logMsg) {
        logger.warn("{} {}", logPrefix, logMsg);
    }

    public void logError(Logger logger, String logPrefix, String logMsg, Exception e) {
        logger.error("{} {}", logPrefix, logMsg, e); // error級別的要印stackTrace
    }

    /**
     * 組織系統通知Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼看效能。
     */
    public String composeLogPrefixForSystem() {
        return this.LOG_PREFIX_FOR_SYSTEM;
    }


    /**
     * 組織排程Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼看效能。
     */
    public String composeLogPrefixForScheduler() {
        return this.LOG_PREFIX_FOR_SCHEDULER;
    }

    /**
     * 組織Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼注重效能。
     *
     * @param userIdOpt  請求者的用戶ID，若業務場景沒有這個資訊，則傳空Optional
     * @param uuid    UUID表示整個請求的生命週期，方便爬LOG。
     */
    public String composeLogPrefixForBusiness(Optional<Integer> userIdOpt, String uuid) {
        return String.format("%s[%s][%s]",
                PART_OF_LOG_PREFIX_FOR_BUSINESS,
                userIdOpt.isEmpty() ? "NULL" : userIdOpt.get(), //有些情況會取不到userId或者不需要特地取userId
                uuid
        );
    }

    /**
     * 製作一個專門給logging的隨機流水號。
     * UUID用於紀錄一個API業務的生命週期，若之後某個API的業務邏輯會需要開多個thread去異步執行，這時就可以用同一個UUID抓出不同thread的log。
     */
    public String generateUUIDForLogging() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

}
