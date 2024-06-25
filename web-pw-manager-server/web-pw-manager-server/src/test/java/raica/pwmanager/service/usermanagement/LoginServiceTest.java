//package raica.pwmanager.service.usermanagement;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import io.jsonwebtoken.*;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import raica.pwmanager.enums.MFAType;
//import raica.pwmanager.dao.extension.impl.UserService;
//import raica.pwmanager.entities.bo.EmailActivationDetail;
//import raica.pwmanager.entities.bo.LoginVerificationCodeWrapper;
//import raica.pwmanager.entities.bo.MyRequestContext;
//import raica.pwmanager.entities.bo.MyUserDetails;
//import raica.pwmanager.entities.cache.MyExpiringConcurrentHashMap;
//import raica.pwmanager.entities.dto.receive.LoginMFAVerificationReqBody;
//import raica.pwmanager.entities.dto.receive.LoginReqBody;
//import raica.pwmanager.entities.dto.receive.RenewAccessTokenReqBody;
//import raica.pwmanager.entities.dto.send.LoginData;
//import raica.pwmanager.entities.dto.send.RenewAccessTokenData;
//import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
//import raica.pwmanager.entities.po.User;
//import raica.pwmanager.exception.MyUnexpectedException;
//import raica.pwmanager.prop.AppInfoProps;
//import raica.pwmanager.prop.ExpirationProps;
//import raica.pwmanager.util.AESUtil;
//import raica.pwmanager.util.JWTUtil;
//import raica.pwmanager.util.MailUtil;
//import raica.pwmanager.util.ResponseUtil;
//
//import java.lang.reflect.Field;
//import java.time.Duration;
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class LoginServiceTest {
//
//    @Mock
//    private RegisterService mockRegisterService;
//
//    @Mock
//    private UserService mockUserService;
//
//    @Mock
//    private ResponseUtil mockResponseUtil;
//
//    @Mock
//    private AESUtil mockAESUtil;
//
//    @Mock
//    private JWTUtil mockJWTUtil;
//
//    @Mock
//    private MailUtil mockMailUtil;
//
//    @Mock
//    private ExpirationProps mockExpirationProps;
//
//    @Mock
//    private AppInfoProps mockAppInfoProps;
//
//
//    @InjectMocks
//    @Spy
//    private LoginService spyLoginService; //待測元件
//
//    private Field userEmailToLoginVerificationCodeWrapperMapField;
//
//    private MyExpiringConcurrentHashMap<String, LoginVerificationCodeWrapper> spyMyExpiringConcurrentHashMap;
//
//
//    /**
//     * 因為某些測項要測試LoginService裡的單例元件MyExpiringConcurrentHashMap，所以為了確保每個測項不互相影響，以及方便追蹤MyExpiringConcurrentHashMap元件，需要進行一些設置。
//     */
//    @BeforeEach
//    void stubComponentBeforeEachTestCase() throws NoSuchFieldException, IllegalAccessException {
//        this.userEmailToLoginVerificationCodeWrapperMapField = LoginService.class.getDeclaredField("userEmailToLoginVerificationCodeWrapperMap");
//        this.userEmailToLoginVerificationCodeWrapperMapField.setAccessible(true);
//        this.spyMyExpiringConcurrentHashMap = Mockito.spy(new MyExpiringConcurrentHashMap<>(Duration.parse("PT1M").toMillis()));
//
//        this.userEmailToLoginVerificationCodeWrapperMapField.set(this.spyLoginService, this.spyMyExpiringConcurrentHashMap); //把loginService實例裡的MyExpiringConcurrentHashMap給Spy化，方便追蹤
//    }
//
//    /**
//     * 每個單元測試過後重置MyExpiringConcurrentHashMap的參考，確保每個測項所使用的MyExpiringConcurrentHashMap一定是互相獨立的。
//     */
//    @AfterEach
//    void resetComponentAfterEachTestCase() throws IllegalAccessException {
//        this.userEmailToLoginVerificationCodeWrapperMapField.set(this.spyLoginService, null);
//        this.userEmailToLoginVerificationCodeWrapperMapField.setAccessible(false);
//    }
//
//
//    @Test
//    void GivenArgs_WhenSendVerificationCodeToUserEmail_ThenExecuteExpectedProcess() {
//        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        ArgumentCaptor<LoginVerificationCodeWrapper> loginVerificationCodeWrapperCaptor = ArgumentCaptor.forClass(LoginVerificationCodeWrapper.class);
//        Mockito.when(mockAppInfoProps.getMailSenderName()).thenReturn("stubMailSenderName");
//
//        spyLoginService.sendVerificationCodeToUserEmail(inputMyUserDetails, inputMyRequestContext);
//
//        Mockito.verify(this.spyMyExpiringConcurrentHashMap, Mockito.times(1)).put(Mockito.eq(inputMyUserDetails.getEmail()), loginVerificationCodeWrapperCaptor.capture());
//        LoginVerificationCodeWrapper targetLoginVerificationCodeWrapper = loginVerificationCodeWrapperCaptor.getValue();
//        Assertions.assertEquals(inputMyRequestContext.getUUID(), targetLoginVerificationCodeWrapper.getVerificationCode());
//        Assertions.assertEquals(inputMyUserDetails, targetLoginVerificationCodeWrapper.getMyUserDetails());
//        Mockito.verify(mockMailUtil, Mockito.times(1)).sendMailTo(inputMyUserDetails.getEmail(), "二階段登入驗證", "您的登入驗證碼為 stubUUID ，請至頁面輸入驗證碼後登入。", "stubMailSenderName");
//    }
//
//    @Test
//    void GivenUserDetailsWhichMFATypeNumMatchesNone_WhenResponseByUserMFAType_ThenReturnExpectedResponseAndStatusCodeIs200() {
//        MFAType stubMfaType = MFAType.NONE;
//        String expectedOutputAccessToken = "stubAccessToken";
//        String expectedOutputRefreshToken = "stubRefreshToken";
//        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, stubMfaType.getTypeNum(), true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        Mockito.when(mockJWTUtil.generateAccessToken(inputMyUserDetails)).thenReturn(expectedOutputAccessToken);
//        Mockito.when(mockJWTUtil.generateRefreshToken(inputMyUserDetails)).thenReturn(expectedOutputRefreshToken);
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.responseByUserMFAType(inputMyUserDetails, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
//        Assertions.assertEquals(inputMyUserDetails, inputMyRequestContext.getMyUserDetailsOpt().get());
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq(""));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertEquals("stubAccessToken", targetLoginData.getAccessToken());
//        Assertions.assertEquals("stubRefreshToken", targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenUserDetailsWhichMFATypeNumMatchesEmail_WhenResponseByUserMFAType_ThenSendVerificationCodeToEmailAndReturnExpectedResponseAndStatusCodeIs202() {
//        MFAType stubMfaType = MFAType.EMAIL;
//        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, stubMfaType.getTypeNum(), true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.responseByUserMFAType(inputMyUserDetails, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.ACCEPTED, actual.getStatusCode());
//        Assertions.assertTrue(inputMyRequestContext.getMyUserDetailsOpt().isEmpty());
//        Mockito.verify(spyLoginService, Mockito.times(1)).sendVerificationCodeToUserEmail(inputMyUserDetails, inputMyRequestContext);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq(String.format("請至 %s 收取驗證碼，進行二階段身份驗證。", stubMfaType.getTypeName())));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetLoginData.getAccessToken());
//        Assertions.assertNull(targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenUserDetailsWhichMFATypeNumMatchesUnknown_WhenResponseByUserMFAType_ThenThrowMyUnexpectedException() {
//        MFAType stubMfaType = MFAType.UNKNOWN;
//        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, stubMfaType.getTypeNum(), true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyLoginService.responseByUserMFAType(inputMyUserDetails, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("User's mfaTypeNum does not match any MFAType enum but it should not occurred.", actual.getMessage());
//    }
//
//    @Test
//    void GivenNormalWorkflow_WhenLogin_ThenExecuteExpectedProcess() {
//        String stubUserInDBPasswordCipherText = "stubUserInDBPasswordCipherText";
//        String stubUserInDBPasswordPlainText = "stubUserInDBPasswordPlainText";
//        boolean stubUserInDBIsActivated = true;
//        LoginReqBody inputLoginReqBody = new LoginReqBody("stubEmail", stubUserInDBPasswordPlainText);
//        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        User stubUser = new User().setId(1).setName("stubName").setMainPassword(stubUserInDBPasswordCipherText).setEmail(inputLoginReqBody.getEmail()).setActivated(stubUserInDBIsActivated);
//        Optional<User> stubUserOpt = Optional.of(stubUser);
//        MyUserDetails stubMyUserDetails = new MyUserDetails(stubUser.getId(), stubUser.getName(), stubUser.getEmail(), stubUser.isActivated(), stubUser.getMfaType(), true, true, true, true, Collections.emptySet());
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(stubUserOpt);
//        Mockito.when(mockAESUtil.decryptFromDB(stubUserOpt.get().getMainPassword())).thenReturn(stubUserInDBPasswordPlainText);
//        Mockito.when(mockJWTUtil.generateMyUserDetailsByUserPo(stubUserOpt.get())).thenReturn(stubMyUserDetails);
//        Mockito.doReturn(ResponseEntity.ok().build()).when(spyLoginService).responseByUserMFAType(Mockito.any(), Mockito.any());
//
//        spyLoginService.login(inputLoginReqBody, stubMyRequestContext);
//
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).generateMyUserDetailsByUserPo(stubUserOpt.get());
//        Mockito.verify(spyLoginService, Mockito.times(1)).responseByUserMFAType(stubMyUserDetails, stubMyRequestContext);
//    }
//
//    @Test
//    void GivenEmailDoesExistInDB_WhenLogin_ThenExecuteExpectedProcessAndStatusCodeIs400() {
//        LoginReqBody inputLoginReqBody = new LoginReqBody("stubEmail", "stubPassword");
//        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        Optional<User> stubUserOpt = Optional.empty();
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(stubUserOpt);
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.login(inputLoginReqBody, stubMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq("郵箱不存在。"));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetLoginData.getAccessToken());
//        Assertions.assertNull(targetLoginData.getRefreshToken());
//    }
//
//
//    @Test
//    void GivenPasswordOfParamIsWrong_WhenLogin_ThenExecuteExpectedProcessAndStatusCodeIs400() {
//        String stubUserInDBPasswordCipherText = "stubUserInDBPasswordCipherText";
//        String stubUserInDBPasswordPlainText = "stubUserInDBPasswordPlainText";
//        boolean stubUserInDBIsActivated = true;
//        LoginReqBody inputLoginReqBody = new LoginReqBody("stubEmail", stubUserInDBPasswordPlainText);
//        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        User stubUser = new User().setId(1).setName("stubName").setMainPassword(stubUserInDBPasswordCipherText).setEmail(inputLoginReqBody.getEmail()).setActivated(stubUserInDBIsActivated);
//        Optional<User> stubUserOpt = Optional.of(stubUser);
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(stubUserOpt);
//        Mockito.when(mockAESUtil.decryptFromDB(stubUserOpt.get().getMainPassword())).thenReturn("stubUserInDBPasswordPlainTextVer2");
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.login(inputLoginReqBody, stubMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq("密碼錯誤。"));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetLoginData.getAccessToken());
//        Assertions.assertNull(targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenUserIsNotActivated_WhenLogin_ThenResendActivationCodeToEmailAndStatusCodeIs400() {
//        String stubUserInDBPasswordCipherText = "stubUserInDBPasswordCipherText";
//        String stubUserInDBPasswordPlainText = "stubUserInDBPasswordPlainText";
//        boolean stubUserInDBIsActivated = false;
//        LoginReqBody inputLoginReqBody = new LoginReqBody("stubEmail", stubUserInDBPasswordPlainText);
//        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        User stubUser = new User().setId(1).setName("stubName").setMainPassword(stubUserInDBPasswordCipherText).setEmail(inputLoginReqBody.getEmail()).setActivated(stubUserInDBIsActivated);
//        Optional<User> stubUserOpt = Optional.of(stubUser);
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(stubUserOpt);
//        Mockito.when(mockAESUtil.decryptFromDB(stubUserOpt.get().getMainPassword())).thenReturn(stubUserInDBPasswordPlainText);
//        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail();
//        Mockito.when(mockRegisterService.generateEmailActivationDetailByUserPo(stubUser)).thenReturn(stubEmailActivationDetail);
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.login(inputLoginReqBody, stubMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Mockito.verify(mockRegisterService, Mockito.times(1)).sendEmailActivationLetter(stubEmailActivationDetail, stubMyRequestContext);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq(String.format("用戶註冊後尚未激活，已重新發送郵件至 %s 郵箱，請點擊信內連結，激活您的帳戶。", stubUser.getEmail())));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetLoginData.getAccessToken());
//        Assertions.assertNull(targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenNormalWorkflow_WhenLoginMFAVerification_ThenExecuteExpectedProcessAndReturnStatusCodeIs200() {
//        String stubUserEmail = "stubEmail";
//        String stubVerificationCode = "stubVerificationCode";
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        LoginMFAVerificationReqBody inputLoginMFAVerificationReqBody = new LoginMFAVerificationReqBody(stubUserEmail, stubVerificationCode);
//        String expectedOutputAccessToken = "stubAccessToken";
//        String expectedOutputRefreshToken = "stubRefreshToken";
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", stubUserEmail, true, 0, true, true, true, true, Collections.emptySet());
//        LoginVerificationCodeWrapper stunLoginVerificationCodeWrapper = new LoginVerificationCodeWrapper(stubVerificationCode, stubMyUserDetails);
//        this.spyMyExpiringConcurrentHashMap.put(stubUserEmail, stunLoginVerificationCodeWrapper);
//        Mockito.when(mockJWTUtil.generateAccessToken(stubMyUserDetails)).thenReturn(expectedOutputAccessToken);
//        Mockito.when(mockJWTUtil.generateRefreshToken(stubMyUserDetails)).thenReturn(expectedOutputRefreshToken);
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.loginMFAVerification(inputLoginMFAVerificationReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
//        Assertions.assertEquals(stubMyUserDetails, inputMyRequestContext.getMyUserDetailsOpt().get());
//        Mockito.verify(this.spyMyExpiringConcurrentHashMap, Mockito.times(1)).removeIfPresent(stubUserEmail);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq(""));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertEquals("stubAccessToken", targetLoginData.getAccessToken());
//        Assertions.assertEquals("stubRefreshToken", targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenEmailDoesNotExistsInLocalCache_WhenLoginMFAVerification_ThenReturnStatusCodeIs400() {
//        String stubUserEmail = "stubEmail";
//        String stubVerificationCode = "stubVerificationCode";
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        LoginMFAVerificationReqBody inputLoginMFAVerificationReqBody = new LoginMFAVerificationReqBody(stubUserEmail, stubVerificationCode);
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.loginMFAVerification(inputLoginMFAVerificationReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Assertions.assertTrue(inputMyRequestContext.getMyUserDetailsOpt().isEmpty());
//        Mockito.verify(this.spyMyExpiringConcurrentHashMap, Mockito.times(0)).removeIfPresent(stubUserEmail);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq("驗證碼已經失效，請重新登入獲取新的驗證碼。"));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetLoginData.getAccessToken());
//        Assertions.assertNull(targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenVerificationCodeOfParamIsWrong_WhenLoginMFAVerification_ThenReturnStatusCodeIs400() {
//        String stubUserEmail = "stubEmail";
//        String stubVerificationCode = "stubVerificationCode";
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        LoginMFAVerificationReqBody inputLoginMFAVerificationReqBody = new LoginMFAVerificationReqBody(stubUserEmail, "wrongVerificationCode");
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", stubUserEmail, true, 0, true, true, true, true, Collections.emptySet());
//        LoginVerificationCodeWrapper stunLoginVerificationCodeWrapper = new LoginVerificationCodeWrapper(stubVerificationCode, stubMyUserDetails);
//        this.spyMyExpiringConcurrentHashMap.put(stubUserEmail, stunLoginVerificationCodeWrapper);
//        ArgumentCaptor<LoginData> loginDataArgumentCaptor = ArgumentCaptor.forClass(LoginData.class);
//
//        ResponseEntity<ResponseBodyTemplate<LoginData>> actual = spyLoginService.loginMFAVerification(inputLoginMFAVerificationReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Assertions.assertTrue(inputMyRequestContext.getMyUserDetailsOpt().isEmpty());
//        Mockito.verify(this.spyMyExpiringConcurrentHashMap, Mockito.times(0)).removeIfPresent(stubUserEmail);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(loginDataArgumentCaptor.capture(), Mockito.eq("驗證碼 wrongVerificationCode 錯誤，請確認驗證碼後重新輸入。"));
//        LoginData targetLoginData = loginDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetLoginData.getAccessToken());
//        Assertions.assertNull(targetLoginData.getRefreshToken());
//    }
//
//    @Test
//    void GivenNormalWorkflow_WhenRenewAccessTokenByRefreshToken_ThenExecuteExpectedProcessAndReturnStatusIs200() {
//        int stubUserIdOfInputRefreshToken = 1;
//        int stubUserIdOfInputAccessToken = 1;
//        RenewAccessTokenReqBody inputRenewAccessTokenReqBody = new RenewAccessTokenReqBody("stubRefreshToken", "stubAccessToken");
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        Jws<Claims> mockRefreshTokenJws = Mockito.mock(Jws.class);
//        Claims stubRefreshTokenPayload = Jwts.claims().add("userId", stubUserIdOfInputRefreshToken).build();
//        Claims stubAccessTokenPayload = Jwts.claims().add("userId", stubUserIdOfInputAccessToken).build();
//        MyUserDetails stubMyUserDetailsConvertedFromAccessTokenPayload = new MyUserDetails(stubUserIdOfInputAccessToken, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        ArgumentCaptor<RenewAccessTokenData> renewAccessTokenDataArgumentCaptor = ArgumentCaptor.forClass(RenewAccessTokenData.class);
//
//        Mockito.when(mockRefreshTokenJws.getPayload()).thenReturn(stubRefreshTokenPayload);
//        Mockito.when(mockJWTUtil.parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken())).thenReturn(mockRefreshTokenJws);
//        Mockito.when(mockJWTUtil.forceParsingExpiredAccessTokenAsClaims(inputRenewAccessTokenReqBody.getAccessToken())).thenReturn(stubAccessTokenPayload);
//        Mockito.when(mockJWTUtil.generateMyUserDetailsByAccessTokenPayload(stubAccessTokenPayload)).thenReturn(stubMyUserDetailsConvertedFromAccessTokenPayload);
//        Mockito.when(mockJWTUtil.generateAccessToken(stubMyUserDetailsConvertedFromAccessTokenPayload)).thenReturn("stubNewAccessToken");
//
//        ResponseEntity<ResponseBodyTemplate<RenewAccessTokenData>> actual = spyLoginService.renewAccessTokenByRefreshToken(inputRenewAccessTokenReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).forceParsingExpiredAccessTokenAsClaims(inputRenewAccessTokenReqBody.getAccessToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).generateAccessToken(stubMyUserDetailsConvertedFromAccessTokenPayload);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(renewAccessTokenDataArgumentCaptor.capture(), Mockito.eq(""));
//        RenewAccessTokenData targetRenewAccessTokenData = renewAccessTokenDataArgumentCaptor.getValue();
//        Assertions.assertEquals("stubNewAccessToken", targetRenewAccessTokenData.getAccessToken());
//    }
//
//    @Test
//    void GivenParseRefreshTokenFailed_WhenRenewAccessTokenByRefreshToken_ThenThrowJWTException() {
//        RenewAccessTokenReqBody inputRenewAccessTokenReqBody = new RenewAccessTokenReqBody("stubRefreshToken", "stubAccessToken");
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//
//        Mockito.when(mockJWTUtil.parseRefreshToken(Mockito.any())).thenThrow(new JwtException("stubMsg"));
//
//        JwtException actual = assertThrows(JwtException.class, () -> {
//            spyLoginService.renewAccessTokenByRefreshToken(inputRenewAccessTokenReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("stubMsg", actual.getMessage());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).forceParsingExpiredAccessTokenAsClaims(inputRenewAccessTokenReqBody.getAccessToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//        Mockito.verify(mockResponseUtil, Mockito.times(0)).generateResponseBodyTemplate(Mockito.any(), Mockito.eq(""));
//    }
//
//    @Test
//    void GivenForceParsingAccessTokenFailed_WhenRenewAccessTokenByRefreshToken_ThenThrowJWTException() {
//        int stubUserIdOfInputRefreshToken = 1;
//        RenewAccessTokenReqBody inputRenewAccessTokenReqBody = new RenewAccessTokenReqBody("stubRefreshToken", "stubAccessToken");
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        Jws<Claims> mockRefreshTokenJws = Mockito.mock(Jws.class);
//        Claims stubRefreshTokenPayload = Jwts.claims().add("userId", stubUserIdOfInputRefreshToken).build();
//
//        Mockito.when(mockRefreshTokenJws.getPayload()).thenReturn(stubRefreshTokenPayload);
//        Mockito.when(mockJWTUtil.parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken())).thenReturn(mockRefreshTokenJws);
//        Mockito.when(mockJWTUtil.forceParsingExpiredAccessTokenAsClaims(Mockito.anyString())).thenThrow(new JwtException("stubMsg"));
//
//        JwtException actual = assertThrows(JwtException.class, () -> {
//            spyLoginService.renewAccessTokenByRefreshToken(inputRenewAccessTokenReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("stubMsg", actual.getMessage());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).forceParsingExpiredAccessTokenAsClaims(inputRenewAccessTokenReqBody.getAccessToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//        Mockito.verify(mockResponseUtil, Mockito.times(0)).generateResponseBodyTemplate(Mockito.any(), Mockito.eq(""));
//    }
//
//    @Test
//    void GivenRefreshTokenDoesNotMatchAccessToken_WhenRenewAccessTokenByRefreshToken_ThenReturnStatusIs400() {
//        int stubUserIdOfInputRefreshToken = 1;
//        int stubUserIdOfInputAccessToken = 2;
//        RenewAccessTokenReqBody inputRenewAccessTokenReqBody = new RenewAccessTokenReqBody("stubRefreshToken", "stubAccessToken");
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
//        Jws<Claims> mockRefreshTokenJws = Mockito.mock(Jws.class);
//        Claims stubRefreshTokenPayload = Jwts.claims().add("userId", stubUserIdOfInputRefreshToken).build();
//        Claims stubAccessTokenPayload = Jwts.claims().add("userId", stubUserIdOfInputAccessToken).build();
//        ArgumentCaptor<RenewAccessTokenData> renewAccessTokenDataArgumentCaptor = ArgumentCaptor.forClass(RenewAccessTokenData.class);
//
//        Mockito.when(mockRefreshTokenJws.getPayload()).thenReturn(stubRefreshTokenPayload);
//        Mockito.when(mockJWTUtil.parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken())).thenReturn(mockRefreshTokenJws);
//        Mockito.when(mockJWTUtil.forceParsingExpiredAccessTokenAsClaims(inputRenewAccessTokenReqBody.getAccessToken())).thenReturn(stubAccessTokenPayload);
//
//        ResponseEntity<ResponseBodyTemplate<RenewAccessTokenData>> actual = spyLoginService.renewAccessTokenByRefreshToken(inputRenewAccessTokenReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseRefreshToken(inputRenewAccessTokenReqBody.getRefreshToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).forceParsingExpiredAccessTokenAsClaims(inputRenewAccessTokenReqBody.getAccessToken());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(renewAccessTokenDataArgumentCaptor.capture(), Mockito.eq("令牌匹配錯誤，請重新登入，或者聯繫我們。請不要提供自己的令牌給第三方人士，或者盜取他人的令牌。"));
//        RenewAccessTokenData targetRenewAccessTokenData = renewAccessTokenDataArgumentCaptor.getValue();
//        Assertions.assertNull(targetRenewAccessTokenData.getAccessToken());
//    }
//
//}