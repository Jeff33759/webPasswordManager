package raica.pwmanager.exception;

/**
 * 當標頭的Authorization沒有存放Token字串時拋出。
 */
public class AuthorizationFieldEmptyException extends CommonException {

    public AuthorizationFieldEmptyException(String message) {
        super(message);
    }

    public AuthorizationFieldEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

}
