package raica.pwmanager.entities.dto.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditTagReqBody {

    @NotBlank(message = "標籤名不能為空。")
    @Size(max = 20, message = "標籤名長度需在20個字以內。")
    private String tagName;

}
