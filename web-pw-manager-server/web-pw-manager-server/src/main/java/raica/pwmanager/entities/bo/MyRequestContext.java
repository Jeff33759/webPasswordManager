package raica.pwmanager.entities.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Optional;

/**
 * 自己做的WebApi用的上下文物件，用來將一個請求的常用數值帶到往後的業務邏輯使用，例如代表一個請求生命週期的UUID，或者是請求者的userId等等......
 * 看是要拿去記Log還是啥的。
 */
@Accessors(chain = true)
@Data
public class MyRequestContext {

    /**
     * 代表一個業務場景生命週期的UUID。
     */
    private String UUID;

    /**
     * 被認證後的憑證，內有用戶的一切資訊。
     * 有一些API需要登入後才能使用，那麼經過認證流程後，後續業務邏輯就可以取用用戶的資訊。
     * 不使用SecurityContextHolder.getContext().getAuthentication，因為反正都做了自己的Context，這樣用起來更方便、更易讀。
     * 若API沒有經過認證流程，那此欄位預設會是空的Optional。
     */
    private Optional<MyUserDetails> myUserDetailsOpt = Optional.empty();

    /**
     * @return 獲取被認證過的用戶ID。如果沒有經過認證流程，則返回空Optional。
     */
    public Optional<Integer> getAuthenticatedUserIdOpt() {
        return this.myUserDetailsOpt.isPresent() ? Optional.of(myUserDetailsOpt.get().getId()) : Optional.empty();
    }

}
