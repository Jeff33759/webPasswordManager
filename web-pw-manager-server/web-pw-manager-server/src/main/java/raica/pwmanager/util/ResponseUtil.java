package raica.pwmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

/**
 * 組織回應格式的工具。
 */
@Component
public class ResponseUtil {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @param data API返回的主要資料
     * @param msg  API提示訊息
     */
    public <D> ResponseBodyTemplate<D> generateResponseBodyTemplate(D data, String msg) {
        return new ResponseBodyTemplate<>(data, msg, Instant.now().toEpochMilli());
    }

    /**
     * 將Json資料寫入HttpServletResponse。
     * {@code response.getWriter()}不可以在這裡flush掉，否則變成commits狀態的response會造成dispatcherServlet跳ERROR。
     *
     * @param pojoCanBeSerializedIntoJson 可以被序列化為Json的物件
     * @throws IOException 當jsonObj轉換成json字串失敗，或者將json字串寫入HttpServletResponse失敗時拋出
     * */
    public void writeJsonResponse(HttpServletResponse response, HttpStatus httpStatus, Object pojoCanBeSerializedIntoJson) throws IOException {
        String jsonStr = objectMapper.writeValueAsString(pojoCanBeSerializedIntoJson);

        response.setStatus(httpStatus.value());
        response.setContentType("application/json");

        response.getWriter().print(jsonStr);
    }

}
