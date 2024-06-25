package raica.pwmanager.exception;

import raica.pwmanager.enums.MyHttpStatus;

public class RegisterException extends BaseBusinessException {

    public RegisterException(MyHttpStatus myHttpStatus, String message) {
        super(myHttpStatus, message);
    }

    public RegisterException(MyHttpStatus myHttpStatus, String message, Throwable cause) {
        super(myHttpStatus, message, cause);
    }
}
