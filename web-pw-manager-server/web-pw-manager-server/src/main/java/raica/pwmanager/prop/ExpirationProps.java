package raica.pwmanager.prop;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * 系統關於有效期限的參數。
 */
@Configuration
public class ExpirationProps {

    /**
     * 一封郵箱激活信的有效期限。
     */
    @Value("${system-items.expiration.email-activation}")
    private String emailActivation;

    /**
     * 新用戶註冊後多久必須進行激活的期限，超過期限會刪除用戶資料
     */
    @Value("${system-items.expiration.user-inactivated}")
    private String userInactivated;

    /**
     * 設置MFA的用戶，必須在登入驗證碼生成後多久以內，完成輸入驗證碼的動作
     */
    @Value("${system-items.expiration.mfa-login-verification}")
    private String mfaLoginVerification;


    @Getter
    private Duration emailActivationExpiration;

    @Getter
    private Duration userInactivatedExpiration;

    @Getter
    private Duration mfaLoginVerificationExpiration;

    /**
     * Spring Bean完成註冊後，初始化一些元件。
     */
    @PostConstruct
    void initializeAfterStartUp() {
        this.emailActivationExpiration = Duration.parse(this.emailActivation);
        this.userInactivatedExpiration = Duration.parse(this.userInactivated);
        this.mfaLoginVerificationExpiration = Duration.parse(this.mfaLoginVerification);
    }


}
