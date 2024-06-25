package raica.pwmanager.enums;

import lombok.Getter;

/**
 * 自訂義的Log類型
 */
@Getter
public enum LogType {

    SYSTEM("SYS"), //系統通知

    BUSINESS("BUSS"), //業務邏輯

    SCHEDULER("SCHE"); // 排程相關

    private final String typeName;

    LogType(String typeName) {
        this.typeName = typeName;
    }


}
