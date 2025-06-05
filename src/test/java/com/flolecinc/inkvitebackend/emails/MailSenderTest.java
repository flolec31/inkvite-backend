package com.flolecinc.inkvitebackend.emails;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSenderTest {

    @Spy
    private MailSender mailSender;

    @Test
    void sendMail_nominal() throws MessagingException {
        // Given
        InternetAddress from = new InternetAddress("from@example.com");
        InternetAddress to = new InternetAddress("to@example.com");
        String subject = "Test Subject";
        String content = "<b>Hello</b>";
        MimeMessage message = mailSender.createMimeMessage();
        doReturn(message).when(mailSender).createMimeMessage();
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        mailSender.sendMail(from, to, subject, content);

        // Then
        assertEquals(subject, message.getSubject());
        assertEquals(from, message.getFrom()[0]);
        assertEquals(to, message.getRecipients(Message.RecipientType.TO)[0]);
        verify(mailSender).send(message);
    }

}