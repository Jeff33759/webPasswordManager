package raica.pwmanager.entities.dto.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raica.pwmanager.validation.annotation.OnlyEnglishAndNumbers;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更改用戶密碼的請求主體。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditUserMainPasswordReqBody {

    @NotBlank(message = "原密碼不能為空。")
    @Size(min = 6, max = 20, message = "原密碼長度需介於6到20個字之間。")
    @OnlyEnglishAndNumbers(message = "原密碼只能輸入英文或數字。")
    private String password;

    @NotBlank(message = "新密碼不能為空。")
    @Size(min = 6, max = 20, message = "新密碼長度需介於6到20個字之間。")
    @OnlyEnglishAndNumbers(message = "新密碼只能輸入英文或數字。")
    private String newPassword;

}
