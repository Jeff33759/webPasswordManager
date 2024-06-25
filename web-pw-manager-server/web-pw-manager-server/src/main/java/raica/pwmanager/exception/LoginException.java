package raica.pwmanager.exception;

import raica.pwmanager.enums.MyHttpStatus;

public class LoginException extends BaseBusinessException{

    public LoginException(MyHttpStatus myHttpStatus, String message) {
        super(myHttpStatus, message);
    }

    public LoginException(MyHttpStatus myHttpStatus, String message, Throwable cause) {
        super(myHttpStatus, message, cause);
    }
}
