package raica.pwmanager.entities.dto.receive;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raica.pwmanager.validation.annotation.OnlyEnglishAndNumbers;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPasswordReqBody {

    @NotNull(message = "分類ID不能為空。")
    private Integer categoryId;

    @NotBlank(message = "密碼標題不能為空。")
    @Size(max = 50, message = "密碼標題限制50字以內。")
    private String title;

    @Size(max = 200, message = "網站連結50字以內。")
    private String webUrl;

    @NotBlank(message = "密碼不能為空。")
    @Size(max = 20, message = "密碼長度需小於20個字。")
    private String password;

    private JsonNode dynamicEntries;

    @Size(max = 500, message = "備註限制500字以內。")
    private String remark;

    private Integer tagId;

}
