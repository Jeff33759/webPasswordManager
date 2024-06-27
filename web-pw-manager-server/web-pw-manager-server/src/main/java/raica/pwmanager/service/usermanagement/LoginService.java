package raica.pwmanager.service.usermanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raica.pwmanager.entities.bo.MyResponseWrapper;
import raica.pwmanager.enums.MFAType;
import raica.pwmanager.dao.extension.impl.UserService;
import raica.pwmanager.entities.bo.LoginVerificationCodeWrapper;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.entities.cache.MyExpiringConcurrentHashMap;
import raica.pwmanager.entities.dto.receive.LoginMFAVerificationReqBody;
import raica.pwmanager.entities.dto.receive.LoginReqBody;
import raica.pwmanager.entities.dto.receive.RenewAccessTokenReqBody;
import raica.pwmanager.entities.dto.send.LoginData;
import raica.pwmanager.entities.dto.send.RenewAccessTokenData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.enums.MyHttpStatus;
import raica.pwmanager.exception.LoginException;
import raica.pwmanager.exception.MyUnexpectedException;
import raica.pwmanager.prop.AppInfoProps;
import raica.pwmanager.prop.ExpirationProps;
import raica.pwmanager.util.AESUtil;
import raica.pwmanager.util.JWTUtil;
import raica.pwmanager.util.MailUtil;
import raica.pwmanager.util.ResponseUtil;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * 登入相關的邏輯。
 */
@Service
public class LoginService {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private AESUtil aesUtil;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private ExpirationProps expirationProps;

    @Autowired
    private AppInfoProps appInfoProps;


    /**
     * 用於登入二階段驗證的本地緩存，可以用email找到對應的UserDetails物件和驗證碼。每個鍵值對都具有時效。
     */
    private MyExpiringConcurrentHashMap<String, LoginVerificationCodeWrapper> userEmailToLoginVerificationCodeWrapperMap;

    /**
     * 空的data欄位，用於註冊API操作失敗時的response。
     * 因為呼叫頻繁，故採用單例，節省系統開銷。
     */
    private final LoginData EMPTY_LOGIN_DATA = new LoginData();


    /**
     * Spring Bean完成註冊後，初始化一些元件。
     */
    @PostConstruct
    void initializeAfterStartUp() {
        this.userEmailToLoginVerificationCodeWrapperMap = new MyExpiringConcurrentHashMap<>(expirationProps.getMfaLoginVerificationExpiration().toMillis());
    }


    public MyResponseWrapper login(LoginReqBody loginReqBody, MyRequestContext myRequestContext) {

        // 1. 根據郵箱從DB撈出User資訊
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("email", loginReqBody.getEmail());

        Optional<User> userOptional = userService.getOneOpt(userQueryWrapper);

        // 2.驗證郵箱
        if (userOptional.isEmpty()) {
            throw new LoginException(MyHttpStatus.ERROR_BAD_REQUEST, "郵箱不存在。");
        }

        User user = userOptional.get();

        // 3. 驗證密碼
        String mainPasswordInDB = aesUtil.decryptFromDB(user.getMainPassword());

        if (!mainPasswordInDB.equals(loginReqBody.getPassword())) {
            throw new LoginException(MyHttpStatus.ERROR_BAD_REQUEST, "密碼錯誤。");
        }

        // 4. 若用戶未激活
        if (!user.isActivated()) {
            // 製作激活信件詳情物件 & 重新發送郵箱激活信件
            registerService.sendEmailActivationLetter(
                    registerService.generateEmailActivationDetailByUserPo(user),
                    myRequestContext
            );

            throw new LoginException(MyHttpStatus.ERROR_BAD_REQUEST, String.format("用戶註冊後尚未激活，已重新發送郵件至 %s 郵箱，請點擊信內連結，激活您的帳戶。", user.getEmail()));
        }

        // 5. 製作授權物件
        MyUserDetails myUserDetails = jwtUtil.generateMyUserDetailsByUserPo(user);

        // 6. 根據用戶設置的mfa類型去返回
        return this.responseByUserMFAType(myUserDetails, myRequestContext);
    }

    /**
     * 根據用戶設置的MFA類型決定登入API的返回。
     *
     * @throws MyUnexpectedException 當User的mfaTypeNum沒有匹配到任一MFAType列舉的時候拋出。通常不會遇到。
     */
    MyResponseWrapper responseByUserMFAType(MyUserDetails myUserDetails, MyRequestContext myRequestContext) throws MyUnexpectedException {
        MFAType mfaType = MFAType.fromTypeNum(myUserDetails.getMfaType());

        return switch (mfaType) {
            case EMAIL -> {
                this.sendVerificationCodeToUserEmail(myUserDetails, myRequestContext);

                ResponseBodyTemplate<LoginData> body = responseUtil.generateResponseBodyTemplate(
                        EMPTY_LOGIN_DATA,
                        String.format("請至 %s 收取驗證碼，進行二階段身份驗證。", mfaType.getTypeName())
                );

                yield new MyResponseWrapper(MyHttpStatus.ACCEPTED, body);
            }
            case NONE -> {
                // 登入成功，將授權物件設置進Context，因Filter chain或interceptor chain可能會用到
                myRequestContext.setMyUserDetailsOpt(Optional.of(myUserDetails));

                ResponseBodyTemplate<LoginData> body = responseUtil.generateResponseBodyTemplate(
                        new LoginData(jwtUtil.generateAccessToken(myUserDetails), jwtUtil.generateRefreshToken(myUserDetails)),
                        ""
                );

                yield new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
            }
            case UNKNOWN ->
                    throw new MyUnexpectedException("User's mfaTypeNum does not match any MFAType enum but it should not occurred."); //通常不該出現，除非DB有人手動改到值。
        };

    }

    /**
     * 發送驗證碼信函至用戶郵箱。
     */
    void sendVerificationCodeToUserEmail(MyUserDetails myUserDetails, MyRequestContext myRequestContext) {
        // 1.生成驗證碼(直接取用UUID)
        String verificationCode = myRequestContext.getUUID();

        // 2.製作本地緩存的物件
        LoginVerificationCodeWrapper loginVerificationCodeWrapper = new LoginVerificationCodeWrapper(verificationCode, myUserDetails);

        // 3. 刷新本地緩存
        userEmailToLoginVerificationCodeWrapperMap.put(myUserDetails.getEmail(), loginVerificationCodeWrapper);

        // 4.寄送信件
        mailUtil.sendMailTo(
                myUserDetails.getEmail(),
                "二階段登入驗證",
                String.format("您的登入驗證碼為 %s ，請至頁面輸入驗證碼後登入。", verificationCode),
                appInfoProps.getMailSenderName()
        );
    }


    public MyResponseWrapper loginMFAVerification(LoginMFAVerificationReqBody loginMFAVerificationReqBody, MyRequestContext myRequestContext) {
        // 1. 檢查驗證碼是否過期
        Optional<LoginVerificationCodeWrapper> loginVerificationCodeWrapperOptional = this.userEmailToLoginVerificationCodeWrapperMap.getOpt(loginMFAVerificationReqBody.getEmail());

        if (loginVerificationCodeWrapperOptional.isEmpty()) {
            throw new LoginException(MyHttpStatus.ERROR_BAD_REQUEST, "驗證碼已經失效，請重新登入獲取新的驗證碼。");
        }

        LoginVerificationCodeWrapper loginVerificationCodeWrapper = loginVerificationCodeWrapperOptional.get();

        // 2.檢查驗證碼是否正確
        if (!loginVerificationCodeWrapper.getVerificationCode().equals(loginMFAVerificationReqBody.getVerificationCode())) {
            throw new LoginException(MyHttpStatus.ERROR_BAD_REQUEST, String.format("驗證碼 %s 錯誤，請確認驗證碼後重新輸入。", loginMFAVerificationReqBody.getVerificationCode()));
        }

        // 3. 清除本地緩存(一個驗證碼只能使用一次)
        this.userEmailToLoginVerificationCodeWrapperMap.removeIfPresent(loginMFAVerificationReqBody.getEmail());

        // 4. 登入成功，將授權物件設置進Context，因Filter chain或interceptor chain可能會用到
        myRequestContext.setMyUserDetailsOpt(Optional.of(loginVerificationCodeWrapper.getMyUserDetails()));

        // 5. 製作Token & 返回
        ResponseBodyTemplate<LoginData> body = responseUtil.generateResponseBodyTemplate(
                new LoginData(jwtUtil.generateAccessToken(loginVerificationCodeWrapper.getMyUserDetails()), jwtUtil.generateRefreshToken(loginVerificationCodeWrapper.getMyUserDetails())),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }

    /**
     * @throws JwtException 若refreshToken失效，或者AccessToken被人為竄改造成解析失敗。
     */
    public MyResponseWrapper renewAccessTokenByRefreshToken(RenewAccessTokenReqBody renewAccessTokenReqBody, MyRequestContext myRequestContext) throws JwtException {
        // 1. 解析refreshToken是否有效
        Claims refreshTokenPayload = jwtUtil.parseRefreshToken(renewAccessTokenReqBody.getRefreshToken()).getPayload();

        // 2. 強制解析accessToken以取得userDetails TODO 如果要加入更即時的黑名單機制，可以去查DB，或是加入緩存機制
        Claims accessTokenPayload = jwtUtil.forceParsingExpiredAccessTokenAsClaims(renewAccessTokenReqBody.getAccessToken());

        // 3. 比對兩個Token的userId是否一致，以免有人盜用別人失效的accessToken加上自己有效的refreshToken，得到別人的accessToken
        if (!refreshTokenPayload.get("userId", Integer.class).equals(accessTokenPayload.get("userId", Integer.class))) {
            throw new LoginException(MyHttpStatus.ERROR_BAD_REQUEST, "令牌匹配錯誤，請重新登入，或者聯繫我們。請不要提供自己的令牌給第三方人士，或者盜取他人的令牌。");
        }

        // 4. 轉換物件
        MyUserDetails myUserDetails = jwtUtil.generateMyUserDetailsByAccessTokenPayload(accessTokenPayload);

        // 5. 製作新的accessToken & 返回
        ResponseBodyTemplate<RenewAccessTokenData> body = responseUtil.generateResponseBodyTemplate(
                new RenewAccessTokenData(jwtUtil.generateAccessToken(myUserDetails)),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }


}
