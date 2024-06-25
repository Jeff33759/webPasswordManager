package raica.pwmanager.cofig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.enums.MyHttpStatus;
import raica.pwmanager.exception.BaseBusinessException;
import raica.pwmanager.util.LogUtil;
import raica.pwmanager.util.ResponseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * 切面程式，攔截各個Controller拋出的例外，處理成預期的回應格式。
 */
@Slf4j
@RestControllerAdvice(basePackages = "raica.pwmanager.controller")
public class ControllerExceptionToResAdvice {

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
    public ResponseEntity handleDataDockingException(BindException ex) {
        FieldError fieldError = ex.getFieldError();
        String errCause = fieldError.getCode();

        if("typeMismatch".equals(errCause)) {
//    		當參數型別匹配錯誤
            return ResponseEntity
                    .status(MyHttpStatus.ERROR_BAD_REQUEST.getCode())
                    .body(
                            responseUtil.generateResponseBodyTemplate(EMPTY_DATA, "請確認資料格式是否正確。")
                    );
        }else {
//    		來自DTO裡面欄位驗證的message
            return ResponseEntity
                    .status(MyHttpStatus.ERROR_BAD_REQUEST.getCode())
                    .body(
                            responseUtil.generateResponseBodyTemplate(EMPTY_DATA, fieldError.getDefaultMessage())
                    );
        }
    }


    /**
     * 攔截有關於參數對接的錯誤，主要攔截使用@Validated驗證時拋出的例外，例如@Pathvariable驗證失敗、QueryString不合預期
     * */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity handleDataDockingException2(ConstraintViolationException ex) {
        return ResponseEntity
                .status(MyHttpStatus.ERROR_BAD_REQUEST.getCode())
                .body(
                        responseUtil.generateResponseBodyTemplate(EMPTY_DATA, ex.getMessage())
                );
    }

    /**
     * 攔截有關於控制器參數匹配錯誤的例外，例如該帶的QueryString卻沒有帶。
     * */
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity handleDataDockingException3(ServletRequestBindingException ex) {
        return ResponseEntity
                .status(MyHttpStatus.ERROR_BAD_REQUEST.getCode())
                .body(
                        responseUtil.generateResponseBodyTemplate(EMPTY_DATA, ex.getMessage())
                );
    }

    /**
     * 攔截刷新AccessToken接口所拋出的JWT例外，可能refreshToken過期或者有人竄改accessToken。
     * */
    @ExceptionHandler(value = JwtException.class)
    public ResponseEntity handleJWTException(JwtException ex) {
        return ResponseEntity
                .status(MyHttpStatus.ERROR_UNAUTHORIZED.getCode())
                .body(
                        responseUtil.generateResponseBodyTemplate(EMPTY_DATA, MyHttpStatus.ERROR_UNAUTHORIZED.getDefaultMsg())
                );
    }

    /**
     * 攔截接口商業邏輯拋出的例外。
     * */
    @ExceptionHandler(value = BaseBusinessException.class)
    public ResponseEntity handleBaseBusinessException(BaseBusinessException ex) {
        return ResponseEntity
                .status(ex.getMyHttpStatus().getCode())
                .body(
                        responseUtil.generateResponseBodyTemplate(EMPTY_DATA, ex.getMessage())
                );
    }

    /**
     * 發生了其他無法預期的例外。
     * */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity handleException(HttpServletRequest request, Exception e) {
        MyRequestContext myReqContext = (MyRequestContext) request.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT);

        logUtil.logError(
                log,
                logUtil.composeLogPrefixForBusiness(myReqContext.getAuthenticatedUserIdOpt(), myReqContext.getUUID()),
                e.getMessage(),
                e
        );

        return ResponseEntity
                .status(MyHttpStatus.ERROR_SYSTEM.getCode())
                .body(
                        responseUtil.generateResponseBodyTemplate(EMPTY_DATA, MyHttpStatus.ERROR_SYSTEM.getDefaultMsg())
                );
    }


}
