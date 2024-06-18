package raica.pwmanager.aop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.util.LogUtil;
import raica.pwmanager.util.ResponseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * 切面程式，攔截各個Controller拋出的例外，處理成預期的回應格式。
 * 這裡沒有攔截到的，就會交給dispatcherServlet處理例外回應。
 */
@Slf4j
@RestControllerAdvice(basePackages = "raica.pwmanager.controller")
public class ControllerExceptionToResAspect {

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private LogUtil logUtil;

    /**
     * 發生錯誤的情況，data欄位統一回應空的Json。
     * 因為呼叫頻繁，故採用單例，節省系統開銷。
     */
    private final JsonNode EMPTY_DATA = JsonNodeFactory.instance.objectNode();


    /**
     * 攔截有關於參數對接的錯誤，例如當資料型別驗證失敗或@Valid驗證失敗。
     * */
    @ExceptionHandler(value = BindException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseBodyTemplate<JsonNode> handleDataDockingException(BindException ex) {
        FieldError fieldError = ex.getFieldError();
        String errCause = fieldError.getCode();

        if("typeMismatch".equals(errCause)) {
//    		當參數型別匹配錯誤
            return responseUtil.generateResponseBodyTemplate(EMPTY_DATA, "請確認資料格式是否正確。");
        }else {
//    		來自DTO裡面欄位驗證的message
            return responseUtil.generateResponseBodyTemplate(EMPTY_DATA, fieldError.getDefaultMessage());
        }
    }


    /**
     * 攔截有關於參數對接的錯誤，主要攔截使用@Validated驗證時拋出的例外，例如@Pathvariable驗證失敗、QueryString不合預期
     * */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseBodyTemplate<JsonNode> handleDataDockingException2(ConstraintViolationException ex) {
        return responseUtil.generateResponseBodyTemplate(EMPTY_DATA, ex.getMessage());
    }

    /**
     * 攔截有關於控制器參數匹配錯誤的例外，例如該帶的QueryString卻沒有帶。
     * */
    @ExceptionHandler(value = ServletRequestBindingException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseBodyTemplate<JsonNode> handleDataDockingException3(ServletRequestBindingException ex) {
        return responseUtil.generateResponseBodyTemplate(EMPTY_DATA, ex.getMessage());
    }

    /**
     * 攔截刷新AccessToken接口所拋出的JWT例外，可能refreshToken過期或者有人竄改accessToken。
     * */
    @ExceptionHandler(value = JwtException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ResponseBodyTemplate<JsonNode> handleJWTException(JwtException ex) {
        return responseUtil.generateResponseBodyTemplate(EMPTY_DATA, "未經認證的請求，請檢查身份憑證是否有效。");
    }

    /**
     * 發生了其他無法預期的例外。
     * */
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBodyTemplate<JsonNode> handleException(HttpServletRequest request, Exception e) {
        MyRequestContext myReqContext = (MyRequestContext) request.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT);

        logUtil.logError(
                log,
                logUtil.composeLogPrefixForBusiness(myReqContext.getAuthenticatedUserIdOpt(), myReqContext.getUUID()),
                e.getMessage(),
                e
        );

        return responseUtil.generateResponseBodyTemplate(EMPTY_DATA, "發生未預期的錯誤，請稍等或者嘗試聯繫我們。");
    }


}
