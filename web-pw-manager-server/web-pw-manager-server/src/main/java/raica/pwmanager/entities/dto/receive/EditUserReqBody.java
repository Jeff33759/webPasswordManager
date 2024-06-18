package raica.pwmanager.entities.dto.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raica.pwmanager.validation.annotation.ValidMFATypeNumber;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更改用戶資訊的請求主體。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditUserReqBody {

    @NotBlank(message = "用戶名稱不能為空。")
    @Size(max = 20, message = "用戶名稱需小於20個字。")
    private String userName;

    @ValidMFATypeNumber(message = "非法的mfaType。")
    private Integer mfaType;

}
