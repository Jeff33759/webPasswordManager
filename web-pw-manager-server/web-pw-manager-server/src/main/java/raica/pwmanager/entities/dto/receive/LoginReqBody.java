package raica.pwmanager.entities.dto.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raica.pwmanager.validation.annotation.OnlyEnglishAndNumbers;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登入API的請求主體。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginReqBody {

    @NotBlank(message = "Email不能為空。")
    @Size(max = 50, message = "Email長度需小於50個字。")
    @Email(message = "郵箱必須為標準email格式。")
    private String email;

    @NotBlank(message = "密碼不能為空。")
    @Size(min = 6, max = 20, message = "密碼長度需介於6到20個字之間。")
    @OnlyEnglishAndNumbers(message = "密碼只能輸入英文或數字。")
    private String password;

}
