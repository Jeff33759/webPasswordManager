package raica.pwmanager.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "system-items.cors")
@Data //要Setter不然ConfigurationProperties無法賦值
public class CorsProps {

    private List<String> allowedOriginsList;

    private List<String> allowedMethodList;

}


