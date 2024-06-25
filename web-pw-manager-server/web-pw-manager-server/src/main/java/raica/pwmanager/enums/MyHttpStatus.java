package raica.pwmanager.enums;

import lombok.Getter;

/**
 * 統一管理返回狀態碼以及默認訊息。
 */
@Getter
public enum MyHttpStatus {

    SUCCESS(200, "success"),
    ACCEPTED(202, "accepted"),
    SUCCESS_NO_CONTENT(204, "NoContent"),
    ERROR_BAD_REQUEST(400, "錯誤的請求。"),
    ERROR_UNAUTHORIZED(401, "未經認證的請求，請檢查身份憑證是否有效。"),
    ERROR_SYSTEM(500, "發生未預期的錯誤，請稍等或者嘗試聯繫我們。");

    private final int code;
    private final String defaultMsg;

    MyHttpStatus(int code, String defaultMsg) {
        this.code = code;
        this.defaultMsg = defaultMsg;
    }
}
