package raica.pwmanager.entities.dto.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data //加上getter，SpringBoot才可以利用Jackson對此物件進行序列化。加上setter，mapstruct才能夠使用
@JsonInclude(JsonInclude.Include.NON_NULL) //若成員變數都沒被賦值，那最後此物件被Jackson序列化時就會是個空JSON
public class QueryPasswordData {

    private int id;

    private int categoryId;

    private String title;

    private String webUrl;

    private String password;

    private JsonNode dynamicEntries;

    private String remark;

    private int tagId;

}
