package com.flolecinc.inkvitebackend.emails;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MailSender extends JavaMailSenderImpl {

    public void sendMail(InternetAddress from, InternetAddress to, String subject, String content) throws MessagingException {
        MimeMessage message = createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        send(message);
    }

}
