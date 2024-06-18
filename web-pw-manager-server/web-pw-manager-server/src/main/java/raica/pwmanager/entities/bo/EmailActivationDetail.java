package raica.pwmanager.entities.bo;

import lombok.*;

/**
 * 激活信件的詳情。
 */
@AllArgsConstructor
@NoArgsConstructor //objectMapper反序列化要這個
@Data
public class EmailActivationDetail {

    private int userId;

    private String userEmail;

    private boolean userIsActivated;

    private long expiration;

}
