package raica.pwmanager.entities.dto.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raica.pwmanager.validation.annotation.JWTFormat;

/**
 * 刷新AccessToken的請求主體。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenewAccessTokenReqBody {

    @JWTFormat(message = "刷新令牌的格式不符，請嘗試重新登入或者聯絡我們。")
    private String refreshToken;

    @JWTFormat(message = "訪問令牌的格式不符，請嘗試重新登入或者聯絡我們。")
    private String accessToken;

}
