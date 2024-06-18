package raica.pwmanager.prop;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 從配置檔讀取用於系統調整的一些參數，統一管理。
 */
@Getter
@Configuration
public class AESProps {

    /**
     * 跟DB互動時的加解密格式。
     */
    @Value("${system-items.encryption.aes.for-db-privacy-data.transformation}")
    private String aesTransformationForDB;

    /**
     * 跟DB互動時的加解密密鑰。
     */
    @Value("${system-items.encryption.aes.for-db-privacy-data.secret-key}")
    private String aesSecretKeyForDB;

    /**
     * 郵箱激活API的隱密資訊的加解密格式。
     */
    @Value("${system-items.encryption.aes.for-email-activation-flow.transformation}")
    private String aesTransformationForEmailActivationFlow;

    /**
     * 郵箱激活API的隱密資訊的加解密密鑰。
     */
    @Value("${system-items.encryption.aes.for-email-activation-flow.secret-key}")
    private String aesSecretKeyForEmailActivationFlow;

}
