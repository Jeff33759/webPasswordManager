package raica.pwmanager.service.usermanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.dao.extension.IUserService;
import raica.pwmanager.entities.bo.EmailActivationDetail;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyResponseWrapper;
import raica.pwmanager.entities.dto.receive.RegisterReqBody;
import raica.pwmanager.entities.dto.send.RegisterData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.enums.MyHttpStatus;
import raica.pwmanager.exception.RegisterException;
import raica.pwmanager.prop.AppInfoProps;
import raica.pwmanager.prop.ExpirationProps;
import raica.pwmanager.util.AESUtil;
import raica.pwmanager.util.LogUtil;
import raica.pwmanager.util.MailUtil;
import raica.pwmanager.util.ResponseUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 *  註冊邏輯服務器。
 */
@Slf4j
@Service
public class RegisterService {

    @Autowired
    private IUserService userService;

    @Autowired
    private AESUtil aesUtil;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private AppInfoProps appInfoProps;

    @Autowired
    private ExpirationProps expirationProps;

    @Autowired
    private RegisterService.RegisterConverter registerConverter;

    @Autowired
    private ObjectMapper objectMapper;


    public MyResponseWrapper register(RegisterReqBody registerReqBody, MyRequestContext myRequestContext) {

        //1. 驗證郵箱是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("email", registerReqBody.getEmail());

        if(userService.exists(userQueryWrapper)) {
            throw new RegisterException(MyHttpStatus.ERROR_BAD_REQUEST, "郵箱已存在。");
        }

        //2. 轉換物件 & 加密敏感資訊
        User user = registerConverter.registerReqBodyToUserPoAndEncryptPrivacyInfo(registerReqBody, aesUtil);

        //3. 新增用戶資料
        userService.save(user);

        //4. 製作激活信件的詳情 & 發送郵箱激活信件
        this.sendEmailActivationLetter(
                this.generateEmailActivationDetailByUserPo(user),
                myRequestContext
        );

        //5. 返回
        ResponseBodyTemplate<RegisterData> body = responseUtil.generateResponseBodyTemplate(
                registerConverter.userPoToRegisterData(user),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }



    public ResponseEntity<String> activateEmail(String mailActivationCode, MyRequestContext myRequestContext) {

        try {
            //1. 解密mailActivationCode(這裡已經不用URLDecode，因為Tomcat已經做完) & 轉換回Dto
            String jsonStr = aesUtil.decryptFromEmailActivationFlow(mailActivationCode);
            EmailActivationDetail emailActivationDetail = objectMapper.readValue(jsonStr, EmailActivationDetail.class);

            //2. 比對時間戳，因為mailActivationCode有設置有效期限
            if(Instant.now().toEpochMilli() >= emailActivationDetail.getExpiration()) { //若mailActivationCode超過有效時限
                //刷新有效時間戳
                emailActivationDetail.setExpiration(Instant.now().plus(expirationProps.getEmailActivationExpiration()).toEpochMilli());

                //重發一次郵箱激活 TODO 這裡之後可以考慮再加個判斷。用戶ID存不存在? 用戶是否已經激活?(以防有激活過的用戶一直點擊那個網址)
                this.sendEmailActivationLetter(emailActivationDetail, myRequestContext);

                return ResponseEntity
                        .badRequest()
                        .body(String.format("<h2>郵箱 %s 激活失敗，因超過激活的有效期限。已再度發送激活郵件，請至郵箱確認。<h2>", emailActivationDetail.getUserEmail()));
            }

            //3. 檢查帳號uid存不存在
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("u_id", emailActivationDetail.getUserId());

            Optional<User> userOptional = userService.getOneOpt(userQueryWrapper);

            if(userOptional.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(String.format("<h2>郵箱 %s 激活失敗，因帳號不存在，請重新註冊。<h2>", emailActivationDetail.getUserEmail()));
            }

            //4. 更改用戶狀態
            User user = userOptional.get()
                    .setActivated(true)
                    .setUpdateTime(new Timestamp(Instant.now().toEpochMilli()));

            userService.updateById(user);

            //5. 返回
            return ResponseEntity
                    .ok(String.format("<h2>郵箱 %s 已激活成功，請關閉本頁面。至首頁登入後，進入系統。<h2>", emailActivationDetail.getUserEmail()));

        } catch (Exception e) {
            // 也許是有人亂改mailActivationCode導致AES解密異常，又或者解密後卻不是預期的json字串。

            logUtil.logWarn(
                    log,
                    logUtil.composeLogPrefixForBusiness(myRequestContext.getAuthenticatedUserIdOpt(), myRequestContext.getUUID()),
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body("<h2>郵箱激活失敗，請重新點擊激活網址，或者聯繫我們。<h2>");
        }
    }


    void sendEmailActivationLetter(EmailActivationDetail emailActivationDetail, MyRequestContext myRequestContext) {
        try {
            //1. 將轉成JSON字串
            String jsonStr = objectMapper.writeValueAsString(emailActivationDetail);

            //2. JSON字串加密 & 進行URL編碼
            String cipherText = aesUtil.encryptForEmailActivationFlow(jsonStr);
            String urlEncodedText = URLEncoder.encode(cipherText, StandardCharsets.UTF_8);

            //3. 組成郵箱激活API的訪問路徑
            String emailActivationFullAddr = appInfoProps.getAccessAddrPrefix() + ApiConst.Path.EMAIL_ACTIVATION + "?mailActivationCode=" + urlEncodedText; //如果上面沒有URLEncode，那麼Tomcat會將base64字串的"+"解析成" "。

            //4. 發送郵箱激活信件
            mailUtil.sendMailTo(
                    emailActivationDetail.getUserEmail(),
                    "請點擊網址激活您的郵箱。",
                    emailActivationFullAddr,
                    appInfoProps.getMailSenderName()
            );
        } catch (Exception e) { //當途中遭遇任何錯誤
            // 印LOG但不中止API繼續執行，因為註冊是成功的，用戶頂多沒收到激活信，之後可以再發一次。
            logUtil.logWarn(
                    log,
                    logUtil.composeLogPrefixForBusiness(myRequestContext.getAuthenticatedUserIdOpt(), myRequestContext.getUUID()),
                    e.getMessage()
            );
        }

    }


    /**
     * 因為有其他服務器會用到，但又不想讓人直接使用RegisterConverter，所以把這段邏輯抽出來，寫成public方法。
     */
    public EmailActivationDetail generateEmailActivationDetailByUserPo(User user) {
        return registerConverter.userPoToEmailActivationDetail(user, expirationProps);
    }

    /**
     * 註冊業務相關的物件轉換器。
     */
    @Mapper(componentModel = "Spring")
    interface RegisterConverter {

        @Mappings({
                @Mapping(target = "name", source = "registerReqBody.userName"),
                @Mapping(target = "email", source = "registerReqBody.email"),
                @Mapping(target = "mainPassword", expression = "java(aesUtil.encryptForDB(registerReqBody.getPassword()))"),
                @Mapping(target = "activated", constant = "false"),
                @Mapping(target = "mfaType", constant = "0"),
                @Mapping(target = "createTime", expression = "java(new java.sql.Timestamp(java.time.Instant.now().toEpochMilli()))"),
                @Mapping(target = "updateTime", expression = "java(new java.sql.Timestamp(java.time.Instant.now().toEpochMilli()))"),
        })
        User registerReqBodyToUserPoAndEncryptPrivacyInfo(RegisterReqBody registerReqBody, @Context AESUtil aesUtil);


        @Mappings({
                @Mapping(target = "userId", source = "userPo.id"),
                @Mapping(target = "userName", source = "userPo.name"),
                @Mapping(target = "isActivated", source = "userPo.activated"),
                @Mapping(target = "email", source = "userPo.email")
        })
        RegisterData userPoToRegisterData(User userPo);


        @Mappings({
                @Mapping(target = "userId", source = "userPo.id"),
                @Mapping(target = "userEmail", source = "userPo.email"),
                @Mapping(target = "userIsActivated", source = "userPo.activated"),
                @Mapping(target = "expiration", expression = "java(java.time.Instant.now().plus(expirationProps.getEmailActivationExpiration()).toEpochMilli())")
        })
        EmailActivationDetail userPoToEmailActivationDetail(User userPo, @Context ExpirationProps expirationProps);

    }

}
