package raica.pwmanager.exception;


/**
 * 和AES加解密有關的例外
 */
public class AESException extends CommonException {

    public AESException(String message) {
        super(message);
    }

    public AESException(String message, Throwable cause) {
        super(message, cause);
    }

}
