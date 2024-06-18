package raica.pwmanager.prop;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 應用程式的一些資訊。
 */
@Getter
@Configuration
public class AppInfoProps {

    /**
     * 應用名稱。
     */
    @Value("${system-items.app-info.name}")
    private String appName;

    /**
     * 訪問API的地址前綴。
     */
    @Value("${system-items.app-info.access-addr-prefix}")
    private String accessAddrPrefix;

    /**
     * 發送郵件時的寄件者姓名。
     */
    @Value("${system-items.app-info.mail-sender-name}")
    private String mailSenderName;

}
