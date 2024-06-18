package raica.pwmanager.entities.dto.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raica.pwmanager.validation.annotation.OnlyEnglishAndNumbers;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登入MFA二階段身份驗證用的DTO。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginMFAVerificationReqBody {

    @NotBlank(message = "Email不能為空。")
    @Size(max = 50, message = "Email長度需小於50個字。")
    @Email(message = "郵箱必須為標準email格式。")
    private String email;

    @Size(min = 6, max = 6, message = "驗證碼長度必須是六個字。")
    @OnlyEnglishAndNumbers(message = "驗證碼只能輸入英文或數字。")
    private String verificationCode;

}
