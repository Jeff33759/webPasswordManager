package raica.pwmanager.service.usermanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.dao.extension.IUserService;
import raica.pwmanager.entities.bo.EmailActivationDetail;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.dto.receive.RegisterReqBody;
import raica.pwmanager.entities.dto.send.RegisterData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.exception.AESException;
import raica.pwmanager.prop.AppInfoProps;
import raica.pwmanager.prop.ExpirationProps;
import raica.pwmanager.util.AESUtil;
import raica.pwmanager.util.LogUtil;
import raica.pwmanager.util.MailUtil;
import raica.pwmanager.util.ResponseUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * 因為有Spring-Validation，所以進到此Service，Dto的參數都會是合法的，不再針對非法參數寫測項。
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private IUserService mockUserService;

    @Mock
    private AESUtil mockAESUtil;

    @Mock
    private ResponseUtil mockResponseUtil;

    @Mock
    private LogUtil mockLogUtil;

    @Mock
    private MailUtil mockMailUtil;

    @Mock
    private AppInfoProps mockAppInfoProps;

    @Mock
    private ExpirationProps mockExpirationProps;

    @Mock
    private RegisterService.RegisterConverter mockRegisterConverter;

    @Mock
    private ObjectMapper mockObjectMapper;

    @InjectMocks
    @Spy
    private RegisterService spyRegisterService; // 待測元件

    @Test
    void GivenNormalWorkflow_WhenRegister_ThenExecuteExpectedProcessAndReturnExpectedBodyContentAndHttpStatusCodeIs200() {
        int stubUserId = 1;
        String stubEmail = "stubEmail@test.test.tw";
        String stubPassword = "stubPassword";
        String stubUserName = "stubUserName";
        boolean stubActivated = false;
        RegisterReqBody inputDto = new RegisterReqBody(stubEmail, stubPassword, stubUserName);
        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
        User stubConvertedUser = new User().setId(stubUserId).setEmail(stubEmail).setName(stubUserName).setActivated(stubActivated);
        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail(stubConvertedUser.getId(), stubConvertedUser.getEmail(), stubConvertedUser.isActivated(), 0L);
        RegisterData stubRegisterData = new RegisterData(stubConvertedUser.getId(), stubConvertedUser.getName(), stubConvertedUser.isActivated(), stubConvertedUser.getEmail());
        ArgumentCaptor<RegisterData> registerDataArgumentCaptor = ArgumentCaptor.forClass(RegisterData.class);

        Mockito.when(mockUserService.exists(Mockito.any(QueryWrapper.class))).thenReturn(false);
        Mockito.when(mockRegisterConverter.registerReqBodyToUserPoAndEncryptPrivacyInfo(inputDto, mockAESUtil)).thenReturn(stubConvertedUser);
        Mockito.when(mockUserService.save(stubConvertedUser)).thenReturn(true);
        Mockito.when(spyRegisterService.generateEmailActivationDetailByUserPo(stubConvertedUser)).thenReturn(stubEmailActivationDetail);
        Mockito.when(mockRegisterConverter.userPoToRegisterData(stubConvertedUser)).thenReturn(stubRegisterData);

        ResponseEntity<ResponseBodyTemplate<RegisterData>> actual = spyRegisterService.register(inputDto, inputMyRequestContext);

        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Mockito.verify(mockUserService, Mockito.times(1)).exists(Mockito.any(QueryWrapper.class)); //一定要先比對郵箱是否存在
        Mockito.verify(mockUserService, Mockito.times(1)).save(stubConvertedUser);
        Mockito.verify(spyRegisterService, Mockito.times(1)).sendEmailActivationLetter(stubEmailActivationDetail, inputMyRequestContext);
        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(registerDataArgumentCaptor.capture(), Mockito.eq(""));
        RegisterData targetRegisterData = registerDataArgumentCaptor.getValue();
        Assertions.assertEquals(stubUserId, targetRegisterData.getUserId());
        Assertions.assertEquals(stubEmail, targetRegisterData.getEmail());
        Assertions.assertEquals(stubUserName, targetRegisterData.getUserName());
        Assertions.assertEquals(stubActivated, targetRegisterData.getIsActivated());
    }


    @Test
    void GivenEmailExistsInDB_WhenRegister_ThenReturnExpectedBodyContentAndHttpStatusCodeIs400() {
        String stubEmail = "stubEmail@test.test.tw";
        String stubPassword = "stubPassword";
        String stubUserName = "stubUserName";
        RegisterReqBody inputDto = new RegisterReqBody(stubEmail, stubPassword, stubUserName);
        MyRequestContext inputMyRequestContext = new MyRequestContext();
        ArgumentCaptor<RegisterData> registerDataArgumentCaptor = ArgumentCaptor.forClass(RegisterData.class);

        Mockito.when(mockUserService.exists(Mockito.any(QueryWrapper.class))).thenReturn(true);

        ResponseEntity<ResponseBodyTemplate<RegisterData>> actual = spyRegisterService.register(inputDto, inputMyRequestContext);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        Mockito.verify(mockUserService, Mockito.times(1)).exists(Mockito.any(QueryWrapper.class)); //一定要先比對郵箱是否存在
        Mockito.verify(mockUserService, Mockito.times(0)).save(Mockito.any(User.class)); //郵箱已存在就不存取DB
        Mockito.verify(spyRegisterService, Mockito.times(0)).sendEmailActivationLetter(Mockito.any(EmailActivationDetail.class), Mockito.eq(inputMyRequestContext));
        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(registerDataArgumentCaptor.capture(), Mockito.eq("郵箱已存在。"));
        RegisterData targetregisterData = registerDataArgumentCaptor.getValue();
        Assertions.assertNull(targetregisterData.getUserId());
        Assertions.assertNull(targetregisterData.getUserName());
        Assertions.assertNull(targetregisterData.getEmail());
        Assertions.assertNull(targetregisterData.getIsActivated());
    }

    @Test
    void GivenNormalWorkflow_WhenSendEmailActivationLetter_ThenPassExpectedArgsToExpectedComponent() throws JsonProcessingException {
        RegisterData stubRegisterData = new RegisterData(1, "stubUserName", false, "stubEmail@test.test.tw");
        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail(stubRegisterData.getUserId(), stubRegisterData.getEmail(), stubRegisterData.getIsActivated(), 0L);
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        String stubJsonStrFromEmailActivationDetail = "{\"userId\":1,\"userEmail\":\"stubUserName\",\"userIsActivated\":false,\"expiration\":0}";
        String stubBase64CipherText = "stubBase64CipherText";
        String stubURLEncodedMailActivationCode = "stubURLEncodedMailActivationCode";
        String stubAccessAddrPrefix = "http://127.0.0.1:8001";
        String stubSenderName = "stubSenderName";
        String expectedEmailActivationFullAddr = stubAccessAddrPrefix + ApiConst.Path.EMAIL_ACTIVATION + String.format("?mailActivationCode=%s", stubURLEncodedMailActivationCode);

        try (MockedStatic<URLEncoder> urlEncoderMockStatic = Mockito.mockStatic(URLEncoder.class)) {
            Mockito.when(mockObjectMapper.writeValueAsString(stubEmailActivationDetail)).thenReturn(stubJsonStrFromEmailActivationDetail);
            Mockito.when(mockAESUtil.encryptForEmailActivationFlow(stubJsonStrFromEmailActivationDetail)).thenReturn(stubBase64CipherText);
            Mockito.when(mockAppInfoProps.getAccessAddrPrefix()).thenReturn(stubAccessAddrPrefix);
            Mockito.when(mockAppInfoProps.getMailSenderName()).thenReturn(stubSenderName);
            urlEncoderMockStatic.when(() -> URLEncoder.encode(stubBase64CipherText, StandardCharsets.UTF_8)).thenReturn(stubURLEncodedMailActivationCode);

            spyRegisterService.sendEmailActivationLetter(stubEmailActivationDetail, stubReqContext);

            Mockito.verify(mockMailUtil, Mockito.times(1)).sendMailTo("stubEmail@test.test.tw", "請點擊網址激活您的郵箱。", expectedEmailActivationFullAddr, stubSenderName);
        }
    }

    @Test
    void GivenJsonProcessingException_WhenSendEmailActivationLetter_ThenOnlyLogWarnLevel() throws JsonProcessingException {
        RegisterData stubRegisterData = new RegisterData(1, "stubUserName", false, "stubEmail@test.test.tw");
        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail(stubRegisterData.getUserId(), stubRegisterData.getEmail(), stubRegisterData.getIsActivated(), 0L);
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");

        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any())).thenThrow(JsonProcessingException.class);
        Mockito.when(mockLogUtil.composeLogPrefixForBusiness(stubReqContext.getAuthenticatedUserIdOpt(), stubReqContext.getUUID())).thenReturn("[NULL][stubUUID]");

        spyRegisterService.sendEmailActivationLetter(stubEmailActivationDetail, stubReqContext);

        Mockito.verify(mockLogUtil, Mockito.times(1)).logWarn(Mockito.any(Logger.class), Mockito.eq("[NULL][stubUUID]"), Mockito.anyString());
        Mockito.verify(mockMailUtil, Mockito.times(0)).sendMailTo(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void GivenAESException_WhenSendEmailActivationLetter_ThenOnlyLogWarnLevel() throws JsonProcessingException {
        RegisterData stubRegisterData = new RegisterData(1, "stubUserName", false, "stubEmail@test.test.tw");
        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail(stubRegisterData.getUserId(), stubRegisterData.getEmail(), stubRegisterData.getIsActivated(), 0L);
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        String stubJsonStr = "{\"data\":{\"userId\":1,\"userName\":\"stubUserName\",\"isActivated\":false,\"email\":\"stubEmail@test.test.tw\"},\"msg\":\"\",\"timestamp\":0}";
        AESException stubAESException = new AESException("stubErrorMsg");

        Mockito.when(mockObjectMapper.writeValueAsString(stubEmailActivationDetail)).thenReturn(stubJsonStr);
        Mockito.when(mockAESUtil.encryptForEmailActivationFlow(stubJsonStr)).thenThrow(stubAESException);
        Mockito.when(mockLogUtil.composeLogPrefixForBusiness(stubReqContext.getAuthenticatedUserIdOpt(), stubReqContext.getUUID())).thenReturn("[NULL][stubUUID]");

        spyRegisterService.sendEmailActivationLetter(stubEmailActivationDetail, stubReqContext);

        Mockito.verify(mockLogUtil, Mockito.times(1)).logWarn(Mockito.any(Logger.class), Mockito.eq("[NULL][stubUUID]"), Mockito.eq("stubErrorMsg"));
        Mockito.verify(mockMailUtil, Mockito.times(0)).sendMailTo(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void GivenNormalWorkflow_WhenActivateEmail_ThenExecuteExpectedProcessAndReturnExpectedHintAndHttpStatusCodeIs200() throws JsonProcessingException {
        User spyUser = Mockito.spy(new User().setId(1).setName("stubUserName").setActivated(false).setEmail("stubEmail"));
        Optional<User> spyUserOptional = Mockito.spy(Optional.of(spyUser));
        String stubMailActivationCode = "stubMailActivationCode";
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        String stubDecryptedJsonStr = "{\"stubKey\":\"stubValue\"}";
        //測試變因1: 郵箱激活碼是否過期
        long stubExp = Instant.now().plus(Duration.ofMinutes(1L)).toEpochMilli(); //沒過期
        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail(spyUser.getId(), spyUser.getEmail(), spyUser.isActivated(), stubExp);

        //測試變因2: AES是否解密成功
        Mockito.when(mockAESUtil.decryptFromEmailActivationFlow(stubMailActivationCode)).thenReturn(stubDecryptedJsonStr); //成功
        //測試變因3: json反序列化是否成功
        Mockito.when(mockObjectMapper.readValue(stubDecryptedJsonStr, EmailActivationDetail.class)).thenReturn(stubEmailActivationDetail); // 成功
        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(spyUserOptional);
        //測試變因4: 玩家資料是否存在
        Mockito.when(spyUserOptional.isEmpty()).thenReturn(false); //存在
        Mockito.doCallRealMethod().when(spyUserOptional).get();

        ResponseEntity<String> actual = spyRegisterService.activateEmail(stubMailActivationCode, stubReqContext);

        Assertions.assertEquals(ResponseEntity.ok("<h2>郵箱 stubEmail 已激活成功，請關閉本頁面。至首頁登入後，進入系統。<h2>"), actual);
        Mockito.verify(spyUser, Mockito.times(1)).setActivated(true);
        Mockito.verify(spyUser, Mockito.times(1)).setUpdateTime(Mockito.any());
        Mockito.verify(mockUserService, Mockito.times(1)).updateById(spyUser);
    }

    @Test
    void GivenEmailActivationExpired_WhenActivateEmail_ThenResendEmailActivationLetterAndReturnExpectedHintAndHttpStatusCodeIs400() throws JsonProcessingException {
        User spyUser = Mockito.spy(new User().setId(1).setName("stubUserName").setActivated(false).setEmail("stubEmail"));
        String stubMailActivationCode = "stubMailActivationCode";
        String stubDecryptedJsonStr = "{\"stubKey\":\"stubValue\"}";
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        Duration stubEmailActivationExpiration = Duration.parse("PT3M");
        //測試變因1: 郵箱激活碼是否過期
        long stubExp = Instant.now().minus(Duration.ofMinutes(1L)).toEpochMilli(); //已過期
        EmailActivationDetail spyEmailActivationDetail = Mockito.spy(new EmailActivationDetail(spyUser.getId(), spyUser.getEmail(), spyUser.isActivated(), stubExp));

        //測試變因2: AES是否解密成功
        Mockito.when(mockAESUtil.decryptFromEmailActivationFlow(stubMailActivationCode)).thenReturn(stubDecryptedJsonStr); //成功
        //測試變因3: json反序列化是否成功
        Mockito.when(mockObjectMapper.readValue(stubDecryptedJsonStr, EmailActivationDetail.class)).thenReturn(spyEmailActivationDetail); //成功
        Mockito.when(mockExpirationProps.getEmailActivationExpiration()).thenReturn(stubEmailActivationExpiration);

        ResponseEntity<String> actual = spyRegisterService.activateEmail(stubMailActivationCode, stubReqContext);

        Assertions.assertEquals(ResponseEntity.badRequest().body("<h2>郵箱 stubEmail 激活失敗，因超過激活的有效期限。已再度發送激活郵件，請至郵箱確認。<h2>"), actual);
        Mockito.verify(spyEmailActivationDetail, Mockito.times(1)).setExpiration(Mockito.anyLong());
        Mockito.verify(spyRegisterService, Mockito.times(1)).sendEmailActivationLetter(spyEmailActivationDetail, stubReqContext);
        Mockito.verify(mockUserService, Mockito.times(0)).updateById(Mockito.any());
    }

    @Test
    void GivenAESException_WhenActivateEmail_ThenLogWarnLevelAndReturnExpectedHintAndHttpStatusCodeIs400() {
        String stubMailActivationCode = "stubMailActivationCode";
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        AESException stubAESException = new AESException("stubMsg");
        //測試變因2: AES是否解密成功
        Mockito.when(mockAESUtil.decryptFromEmailActivationFlow(stubMailActivationCode)).thenThrow(stubAESException); //失敗
        Mockito.when(mockLogUtil.composeLogPrefixForBusiness(stubReqContext.getAuthenticatedUserIdOpt(), stubReqContext.getUUID())).thenReturn("[NULL][stubUUID]");

        ResponseEntity<String> actual = spyRegisterService.activateEmail(stubMailActivationCode, stubReqContext);

        Assertions.assertEquals(ResponseEntity.badRequest().body("<h2>郵箱激活失敗，請重新點擊激活網址，或者聯繫我們。<h2>"), actual);
        Mockito.verify(mockLogUtil, Mockito.times(1)).logWarn(Mockito.any(Logger.class), Mockito.eq("[NULL][stubUUID]"), Mockito.anyString());
        Mockito.verify(mockUserService, Mockito.times(0)).updateById(Mockito.any());
    }

    @Test
    void GivenDeserializeJsonIntoEmailActivationDetailFailed_WhenActivateEmail_ThenThenReturnExpectedHintAndHttpStatusCodeIs400() throws JsonProcessingException {
        String stubMailActivationCode = "stubMailActivationCode";
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        String stubDecryptedJsonStr = "{\"stubKey\":\"stubValue\"}";

        //測試變因2: AES是否解密成功
        Mockito.when(mockAESUtil.decryptFromEmailActivationFlow(stubMailActivationCode)).thenReturn(stubDecryptedJsonStr); //成功
        //測試變因3: json反序列化是否成功
        Mockito.when(mockObjectMapper.readValue(stubDecryptedJsonStr, EmailActivationDetail.class)).thenThrow(JsonProcessingException.class); // 失敗
        Mockito.when(mockLogUtil.composeLogPrefixForBusiness(stubReqContext.getAuthenticatedUserIdOpt(), stubReqContext.getUUID())).thenReturn("[NULL][stubUUID]");

        ResponseEntity<String> actual = spyRegisterService.activateEmail(stubMailActivationCode, stubReqContext);

        Assertions.assertEquals(ResponseEntity.badRequest().body("<h2>郵箱激活失敗，請重新點擊激活網址，或者聯繫我們。<h2>"), actual);
        Mockito.verify(mockLogUtil, Mockito.times(1)).logWarn(Mockito.any(Logger.class), Mockito.eq("[NULL][stubUUID]"), Mockito.anyString());
        Mockito.verify(mockUserService, Mockito.times(0)).updateById(Mockito.any());
    }

    @Test
    void GivenUserDoesNotExistInDB_WhenActivateEmail_ThenReturnExpectedHintAndHttpStatusCodeIs400() throws JsonProcessingException {
        User spyUser = Mockito.spy(new User().setId(1).setName("stubUserName").setActivated(false).setEmail("stubEmail"));
        Optional<User> spyUserOptional = Mockito.spy(Optional.of(spyUser));
        String stubMailActivationCode = "stubMailActivationCode";
        MyRequestContext stubReqContext = new MyRequestContext().setUUID("stubUUID");
        String stubDecryptedJsonStr = "{\"stubKey\":\"stubValue\"}";
        //測試變因1: 郵箱激活碼是否過期
        long stubExp = Instant.now().plus(Duration.ofMinutes(1L)).toEpochMilli(); //沒過期
        EmailActivationDetail stubEmailActivationDetail = new EmailActivationDetail(spyUser.getId(), spyUser.getEmail(), spyUser.isActivated(), stubExp);

        //測試變因2: AES是否解密成功
        Mockito.when(mockAESUtil.decryptFromEmailActivationFlow(stubMailActivationCode)).thenReturn(stubDecryptedJsonStr); //成功
        //測試變因3: json反序列化是否成功
        Mockito.when(mockObjectMapper.readValue(stubDecryptedJsonStr, EmailActivationDetail.class)).thenReturn(stubEmailActivationDetail); // 成功
        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(spyUserOptional);
        //測試變因4: 玩家資料是否存在
        Mockito.when(spyUserOptional.isEmpty()).thenReturn(true); //不存在

        ResponseEntity<String> actual = spyRegisterService.activateEmail(stubMailActivationCode, stubReqContext);

        Assertions.assertEquals(ResponseEntity.badRequest().body("<h2>郵箱 stubEmail 激活失敗，因帳號不存在，請重新註冊。<h2>"), actual);
        Mockito.verify(mockUserService, Mockito.times(0)).updateById(Mockito.any());
    }

    @Test
    void GivenUserPo_WhenGenerateEmailActivationDetailByUserPo_ThenInvokeRegisterConverter() {
        User stubUser = new User();

        spyRegisterService.generateEmailActivationDetailByUserPo(stubUser);

        Mockito.verify(mockRegisterConverter, Mockito.times(1)).userPoToEmailActivationDetail(stubUser, mockExpirationProps);
    }

}