package raica.pwmanager.entities.bo;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用於MFA登入驗證碼的包裹器，新增了對應的UserDetails物件用於製作Token。
 */
@AllArgsConstructor
@Getter
public class LoginVerificationCodeWrapper {

    private String verificationCode;

    private MyUserDetails myUserDetails;

}
