package raica.pwmanager.exception;

/**
 * 所有系統自訂義例外的父類。
 */
public class CommonException extends RuntimeException {

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }

}
