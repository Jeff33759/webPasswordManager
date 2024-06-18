package raica.pwmanager.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import raica.pwmanager.exception.AESException;
import raica.pwmanager.prop.AESProps;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * AES加解密工具包。
 */
@Component
public class AESUtil {

    @Autowired
    private AESProps AESProps;

    /**
     * runtime暫時不會有更動需求，所以密鑰物件使用單例，不必每次都重新生成。
     */
    private SecretKey secretKeyForDB;

    /**
     * runtime暫時不會有更動需求，所以密鑰物件使用單例，不必每次都重新生成。
     */
    private SecretKey secretKeyForEmailActivationFlow;

    /**
     * BouncyCastle是開源的第三方演算法提供商，提供一些Java標準函式庫沒有提供的哈希演算法或加密演算法，例如AES的PKCS7填充模式。
     * 使用第三方演算法，需要通過Security.addProvider()。
     * 寫在static區塊，於程式啟動時註冊BouncyCastleProvider。
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Spring Bean完成註冊後，初始化一些元件。
     */
    @PostConstruct
    void initializeAfterStartUp() {
        this.secretKeyForDB = new SecretKeySpec(AESProps.getAesSecretKeyForDB().getBytes(), AESProps.getAesTransformationForDB());
        this.secretKeyForEmailActivationFlow = new SecretKeySpec(AESProps.getAesSecretKeyForEmailActivationFlow().getBytes(), AESProps.getAesTransformationForEmailActivationFlow());
    }

    /**
     * thread-safe。
     *
     * @throws AESException 當加密任一環節出錯，則拋出
     */
    public String encryptForDB(String plainText) throws AESException {
        try {
            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance(this.AESProps.getAesTransformationForDB()); //Cipher不是thread-safe，所以每次都要getInstance
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKeyForDB);

            return Base64.getEncoder().encodeToString(cipher.doFinal(plainTextBytes));
        } catch (Exception e) {
            throw new AESException("AES encryption for DB failed.", e);
        }
    }

    /**
     * thread-safe。
     *
     * @throws AESException 當解密任一環節出錯，則拋出
     */
    public String decryptFromDB(String base64EncodedCipherText) throws AESException {
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(base64EncodedCipherText);

            Cipher cipher = Cipher.getInstance(this.AESProps.getAesTransformationForDB()); //Cipher不是thread-safe，所以每次都要getInstance
            cipher.init(Cipher.DECRYPT_MODE, this.secretKeyForDB);

            return new String(cipher.doFinal(cipherTextBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AESException("AES decryption from DB failed.", e);
        }
    }

    /**
     * thread-safe。
     *
     * @throws AESException 當加密任一環節出錯，則拋出
     */
    public String encryptForEmailActivationFlow(String plainText) throws AESException {
        try {
            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance(this.AESProps.getAesTransformationForEmailActivationFlow()); //Cipher不是thread-safe，所以每次都要getInstance
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKeyForEmailActivationFlow);

            return Base64.getEncoder().encodeToString(cipher.doFinal(plainTextBytes));
        } catch (Exception e) {
            throw new AESException("AES encryption for e-mail auth flow failed.", e);
        }
    }

    /**
     * thread-safe。
     *
     * @throws AESException 當解密任一環節出錯，則拋出
     */
    public String decryptFromEmailActivationFlow(String base64EncodedCipherText) throws AESException {
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(base64EncodedCipherText);

            Cipher cipher = Cipher.getInstance(this.AESProps.getAesTransformationForEmailActivationFlow()); //Cipher不是thread-safe，所以每次都要getInstance
            cipher.init(Cipher.DECRYPT_MODE, this.secretKeyForEmailActivationFlow);

            return new String(cipher.doFinal(cipherTextBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AESException("AES decryption from e-mail auth flow failed.", e);
        }
    }

}
