package raica.pwmanager.consts;

import lombok.NoArgsConstructor;

/**
 * 集中管理一些常用的Exception message模板。
 */
@NoArgsConstructor
public class MyExceptionMsgTemplate {

    /**
     * 當訪問受保護API，卻沒有經過Security chain造成報錯。
     */
    public static final String UNAUTHENTICATED_USER = "User has to been authenticated but has not.";

}
