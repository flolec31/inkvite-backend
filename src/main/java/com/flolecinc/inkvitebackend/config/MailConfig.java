package com.flolecinc.inkvitebackend.config;

import com.flolecinc.inkvitebackend.emails.MailSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Bean
    public MailSender mailSender() {
        return new MailSender();
    }

}
