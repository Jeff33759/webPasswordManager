package raica.pwmanager.entities.dto.receive;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 有一種情況，是欄位原本有值，但使用者想把它清空。所以不使用動態SQL，轉而強制令前端一定要傳值。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditPasswordReqBody {


    @NotBlank(message = "密碼標題不能為空。")
    @Size(max = 50, message = "密碼標題限制50字以內。")
    private String title;

    @NotNull(message = "webUrl不能為null。")
    @Size(max = 200, message = "網站連結50字以內。")
    private String webUrl;

    @NotBlank(message = "密碼不能為空。")
    @Size(max = 20, message = "密碼長度需小於20個字。")
    private String password;

    @NotNull(message = "動態欄位不能為空。")
    private JsonNode dynamicEntries;

    @NotNull(message = "備註不能為空。")
    @Size(max = 500, message = "備註限制500字以內。")
    private String remark;

    @NotNull(message = "標籤ID不能為空。")
    private Integer tagId;

}
