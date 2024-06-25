package raica.pwmanager.exception;

import lombok.Getter;
import raica.pwmanager.enums.MyHttpStatus;

/**
 * 業務邏輯相關例外的基類。
 * 業務邏輯=和認證授權等等無關的接口商業邏輯。
 */
@Getter
public class BaseBusinessException extends CommonException {

    final MyHttpStatus myHttpStatus;

    public BaseBusinessException(MyHttpStatus myHttpStatus, String message) {
        super(message);
        this.myHttpStatus = myHttpStatus;
    }

    public BaseBusinessException(MyHttpStatus myHttpStatus, String message, Throwable cause) {
        super(message, cause);
        this.myHttpStatus = myHttpStatus;
    }
}
