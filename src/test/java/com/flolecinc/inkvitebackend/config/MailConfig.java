package com.flolecinc.inkvitebackend.config;

import com.flolecinc.inkvitebackend.emails.MailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class MailConfig {

    @Bean
    public MailSender mailSender() {
        return mock(MailSender.class);
    }

}
