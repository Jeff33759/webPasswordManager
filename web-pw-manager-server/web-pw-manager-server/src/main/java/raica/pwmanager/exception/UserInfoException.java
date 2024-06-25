package raica.pwmanager.exception;

import raica.pwmanager.enums.MyHttpStatus;

public class UserInfoException extends BaseBusinessException{
    public UserInfoException(MyHttpStatus myHttpStatus, String message) {
        super(myHttpStatus, message);
    }

    public UserInfoException(MyHttpStatus myHttpStatus, String message, Throwable cause) {
        super(myHttpStatus, message, cause);
    }
}
