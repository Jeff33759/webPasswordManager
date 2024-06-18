package raica.pwmanager.controller;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.dto.receive.LoginMFAVerificationReqBody;
import raica.pwmanager.entities.dto.receive.LoginReqBody;
import raica.pwmanager.entities.dto.receive.RegisterReqBody;
import raica.pwmanager.entities.dto.receive.RenewAccessTokenReqBody;
import raica.pwmanager.entities.dto.send.LoginData;
import raica.pwmanager.entities.dto.send.RegisterData;
import raica.pwmanager.entities.dto.send.RenewAccessTokenData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.service.usermanagement.LoginService;
import raica.pwmanager.service.usermanagement.RegisterService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;


/**
 * 認證相關的API，含註冊、登入。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AuthenticationController {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private LoginService loginService;


    @PostMapping(path = ApiConst.Path.REGISTER)
    public ResponseEntity<ResponseBodyTemplate<RegisterData>> register(@Valid @RequestBody RegisterReqBody registerReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return registerService.register(registerReqBody, myReqContext);
    }

    @GetMapping(path = ApiConst.Path.EMAIL_ACTIVATION, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> emailActivation(@NotBlank(message = "郵箱激活碼不得為空。") @RequestParam("mailActivationCode") String mailActivationCode, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return registerService.activateEmail(mailActivationCode, myReqContext);
    }

    @PostMapping(path = ApiConst.Path.LOGIN)
    public ResponseEntity<ResponseBodyTemplate<LoginData>> login(@Valid @RequestBody LoginReqBody loginReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return loginService.login(loginReqBody, myReqContext);
    }

    @PostMapping(path = ApiConst.Path.LOGIN_MFA_VERIFICATION)
    public ResponseEntity<ResponseBodyTemplate<LoginData>> loginMFAVerification(@Valid @RequestBody LoginMFAVerificationReqBody loginMFAVerificationReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return loginService.loginMFAVerification(loginMFAVerificationReqBody, myReqContext);
    }

    @PostMapping(path = ApiConst.Path.RENEW_ACCESS_TOKEN)
    public ResponseEntity<ResponseBodyTemplate<RenewAccessTokenData>> renewAccessToken(@Valid @RequestBody RenewAccessTokenReqBody renewAccessTokenReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) throws JwtException {
        return loginService.renewAccessTokenByRefreshToken(renewAccessTokenReqBody, myReqContext);
    }


}
