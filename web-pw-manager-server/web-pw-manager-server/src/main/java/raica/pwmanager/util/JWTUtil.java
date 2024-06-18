package raica.pwmanager.util;

import io.jsonwebtoken.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.prop.AppInfoProps;
import raica.pwmanager.prop.JWTProps;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;


@Component
public class JWTUtil {

    @Autowired
    private JWTProps jwtProps;

    @Autowired
    private AppInfoProps appInfoProps;

    @Autowired
    private AuthenticationConverter authenticationConverter;

    private JwtParser accessTokenParser; // thread-safe

    private JwtParser refreshTokenParser; // thread-safe

    /**
     * Spring Bean完成註冊後，初始化一些元件。
     */
    @PostConstruct
    void initializeAfterStartUp() {
        this.accessTokenParser = Jwts.parser().verifyWith(jwtProps.getAccessTokenSecretKey()).build();
        this.refreshTokenParser = Jwts.parser().verifyWith(jwtProps.getRefreshTokenSecretKey()).build();
    }


    /**
     * @throws JwtException 當Token解析失敗時拋出。
     */
    public Jws<Claims> parseAccessToken(String accessToken) throws JwtException {
        return this.accessTokenParser.parseSignedClaims(accessToken);
    }

    /**
     * 強制解析AccessToken的Claims，不管是否過期。
     * @throws JwtException 當Token解析失敗時拋出(當遭遇除了過期以外的例外)。
     */
    public Claims forceParsingExpiredAccessTokenAsClaims(String accessToken) throws JwtException {
        Claims claims;

        try {
            claims = this.accessTokenParser.parseSignedClaims(accessToken).getPayload();
        } catch (ExpiredJwtException eje) {
            claims = eje.getClaims();
        }

        return claims;
    }

    /**
     * @throws JwtException 當Token解析失敗時拋出。
     */
    public Jws<Claims> parseRefreshToken(String refreshToken) throws JwtException {
        return this.refreshTokenParser.parseSignedClaims(refreshToken);
    }

    /**
     * @param myUserDetails 自己做的SpringSecurity身份憑證。製作Token用這個，別用User Po，方便以後做權杖功能擴充。
     */
    public String generateAccessToken(MyUserDetails myUserDetails) {
        Instant expTime = Instant.now().plus(jwtProps.getAccessTokenExpiration());

//      AccessToken公開內容需要放比較多的個人資訊(除了密碼)，取代Session，方便客戶端取用
        Claims claims = Jwts.claims()
                .expiration(Date.from(expTime))
                .issuer(appInfoProps.getAppName())
                .subject("Access token.")
                .add("userId", myUserDetails.getId())
                .add("userName", myUserDetails.getName())
                .add("email", myUserDetails.getEmail())
                .add("activated", myUserDetails.getActivated())
                .add("mfaType", myUserDetails.getMfaType()).build();

        return Jwts.builder()
                .header().add("alg", Jwts.SIG.HS256.getId()).add("typ", "JWT")
                .and()
                .claims(claims)
                .signWith(jwtProps.getAccessTokenSecretKey()) // 密鑰簽章
                .compact();
    }

    /**
     * @param myUserDetails 自己做的SpringSecurity身份憑證。製作Token用這個，別用User Po，方便以後做權杖功能擴充。
     */
    public String generateRefreshToken(MyUserDetails myUserDetails) {
        Instant expTime = Instant.now().plus(jwtProps.getRefreshTokenExpiration());

        Claims claims = Jwts.claims()
                .expiration(Date.from(expTime))
                .issuer(appInfoProps.getAppName())
                .subject("Refresh token.")
                .add("userId", myUserDetails.getId())
                .build();

        return Jwts.builder()
                .header().add("alg", Jwts.SIG.HS256.getId()).add("typ", "JWT")
                .and()
                .claims(claims)
                .signWith(jwtProps.getRefreshTokenSecretKey()) // 密鑰簽章
                .compact();
    }


    /**
     * 有其他元件會用到，但又不想讓人直接使用AuthenticationConverter，所以把這段邏輯抽出來，寫成public方法。
     */
    public MyUserDetails generateMyUserDetailsByAccessTokenPayload(Claims accessTokenPayload) {
        return authenticationConverter.accessTokenPayloadToMyUserDetailsBo(accessTokenPayload);
    }

    /**
     * 有其他元件會用到，但又不想讓人直接使用AuthenticationConverter，所以把這段邏輯抽出來，寫成public方法。
     */
    public MyUserDetails generateMyUserDetailsByUserPo(User userPo) {
        return authenticationConverter.userPoToMyUserDetailsBo(userPo);
    }

    /**
     * 身份憑證相關的轉換器。
     */
    @Mapper(componentModel = "Spring")
    interface AuthenticationConverter {

        @Mappings({
                @Mapping(target = "id", expression = "java(accessTokenPayload.get(\"userId\", Integer.class))"),
                @Mapping(target = "name", expression = "java(accessTokenPayload.get(\"userName\", String.class))"),
                @Mapping(target = "email", expression = "java(accessTokenPayload.get(\"email\", String.class))"),
                @Mapping(target = "activated", expression = "java(accessTokenPayload.get(\"activated\", Boolean.class))"),
                @Mapping(target = "mfaType", expression = "java(accessTokenPayload.get(\"mfaType\", Integer.class))"),
                @Mapping(target = "enabled", constant = "true"),
                @Mapping(target = "accountNonExpired", constant = "true"),
                @Mapping(target = "credentialsNonExpired", constant = "true"),
                @Mapping(target = "accountNonLocked", constant = "true"),
                @Mapping(target = "authorities", expression = "java(java.util.Collections.emptySet())") //暫時不做角色權杖管理
        })
        MyUserDetails accessTokenPayloadToMyUserDetailsBo(Claims accessTokenPayload);


        @Mappings({
                @Mapping(target = "id", source = "user.id"),
                @Mapping(target = "name", source = "user.name"),
                @Mapping(target = "email", source = "user.email"),
                @Mapping(target = "activated", source = "user.activated"),
                @Mapping(target = "mfaType", source = "user.mfaType"),
                @Mapping(target = "enabled", constant = "true"),
                @Mapping(target = "accountNonExpired", constant = "true"),
                @Mapping(target = "credentialsNonExpired", constant = "true"),
                @Mapping(target = "accountNonLocked", constant = "true"),
                @Mapping(target = "authorities", expression = "java(java.util.Collections.emptySet())") //暫時不做角色權杖管理
        })
        MyUserDetails userPoToMyUserDetailsBo(User user);

    }

}
