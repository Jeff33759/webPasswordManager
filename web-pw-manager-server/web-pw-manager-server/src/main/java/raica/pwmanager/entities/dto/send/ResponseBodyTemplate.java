package raica.pwmanager.entities.dto.send;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter //加上getter，SpringBoot才可以利用Jackson對此物件進行序列化
public class ResponseBodyTemplate<D> {

    /**
     * API主體資料。
     * 若無，則回空Json。
     */
    private D data;

    /**
     * API提示信息。
     * 大多時候用於描述請求失敗的原因。
     * <p>
     * 若不存在，則為空字串。
     */
    private String msg;

    /**
     * 返回時間戳。
     */
    @Setter
    private long timestamp;

}
