package raica.pwmanager.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender; // 只要別在runTime去改SMTP的參數，就是thread-safe

    /**
     * @param targetEmail 目標郵箱地址
     * @param subject     主旨
     * @param content     內容
     * @param senderName  寄件者名稱
     */
    public void sendMailTo(String targetEmail, String subject, String content, String senderName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(targetEmail);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom(senderName);

        mailSender.send(message);
    }

}
