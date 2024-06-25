package raica.pwmanager.cofig;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import raica.pwmanager.entities.bo.MyResponseWrapper;
import raica.pwmanager.enums.MyHttpStatus;

/**
 * 統一處理成功的回應。
 */
@RestControllerAdvice(basePackages = "raica.pwmanager.controller")
public class MySuccessfulResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @ResponseBody
    public Object beforeBodyWrite(Object returnFromController, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (returnFromController instanceof MyResponseWrapper wrapper) {
            MyHttpStatus myHttpStatus = wrapper.getMyHttpStatus();

            if (myHttpStatus == MyHttpStatus.SUCCESS_NO_CONTENT) {
                response.setStatusCode(HttpStatus.NO_CONTENT);
                return null;
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(myHttpStatus.getCode());

                response.setStatusCode(httpStatus);

                return wrapper.getBody();
            }
        }

        return returnFromController; //激活用戶的相關接口，需要返回字串。
    }
}
