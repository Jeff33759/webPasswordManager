package raica.pwmanager.prop;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Duration;

/**
 * JWT相關的參數。
 */
@Configuration
public class JWTProps {

    @Value("${system-items.jwt.access-token.secret-key}")
    private String accessTokenSecretKeyStr;

    @Value("${system-items.jwt.access-token.expiration}")
    private String accessTokenExpirationStr;

    @Value("${system-items.jwt.refresh-token.secret-key}")
    private String refreshTokenSecretKeyStr;

    @Value("${system-items.jwt.refresh-token.expiration}")
    private String refreshTokenExpirationStr;


    @Getter
    private SecretKey accessTokenSecretKey;

    @Getter
    private Duration accessTokenExpiration;

    @Getter
    private SecretKey refreshTokenSecretKey;

    @Getter
    private Duration refreshTokenExpiration;


    /**
     * Spring Bean完成註冊後，初始化一些元件。
     */
    @PostConstruct
    void initializeAfterStartUp() {
        this.accessTokenSecretKey = Keys.hmacShaKeyFor(this.accessTokenSecretKeyStr.getBytes());
        this.accessTokenExpiration = Duration.parse(this.accessTokenExpirationStr);

        this.refreshTokenSecretKey = Keys.hmacShaKeyFor(this.refreshTokenSecretKeyStr.getBytes());
        this.refreshTokenExpiration = Duration.parse(this.refreshTokenExpirationStr);
    }

}
