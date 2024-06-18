package raica.pwmanager.exception;

/**
 * 未設想到的例外，例如某些不該出現在生產環境的錯誤。
 */
public class MyUnexpectedException extends CommonException {

    public MyUnexpectedException(String message) {
        super(message);
    }

    public MyUnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

}
