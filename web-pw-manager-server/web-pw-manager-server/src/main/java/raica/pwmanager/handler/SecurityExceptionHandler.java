package raica.pwmanager.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import raica.pwmanager.cofig.SecurityConfig;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.util.ResponseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 客製化Security請求認證的進入點，以及授權訪問失敗的行為。
 *
 * @see SecurityConfig
 */
@Slf4j
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint {

    @Autowired
    private ResponseUtil responseUtil;

    /**
     * 空的data欄位，因為呼叫頻繁，故採用單例，節省系統開銷。
     */
    private final JsonNode EMPTY_DATA = JsonNodeFactory.instance.objectNode();

    /**
     * 當訪問者未通過認證(登入失敗)時的處理。
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseBodyTemplate<JsonNode> jsonNodeResponseBodyTemplate = responseUtil.generateResponseBodyTemplate(EMPTY_DATA, "未經認證的請求，請檢查身份憑證或請求URL是否正確。");

        responseUtil.writeJsonResponse(response, HttpStatus.UNAUTHORIZED, jsonNodeResponseBodyTemplate);
    }

}
