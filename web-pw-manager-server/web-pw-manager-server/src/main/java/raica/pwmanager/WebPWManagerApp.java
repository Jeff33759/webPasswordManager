package raica.pwmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //開啟排程
public class WebPWManagerApp {
    public static void main(String[] args) {
        SpringApplication.run(WebPWManagerApp.class, args);
    }
}