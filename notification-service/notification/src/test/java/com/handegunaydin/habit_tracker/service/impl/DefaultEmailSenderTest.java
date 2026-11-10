package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultEmailSenderTest {
    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private DefaultEmailSender defaultEmailSender;

    @Test
    void send_shouldCreateAndSendEmailMessage() throws MessagingException {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        EmailMessage emailMessage = new EmailMessage("from@from.com", "to@to.com", "subject", "body");
        defaultEmailSender.send(emailMessage);
        verify(javaMailSender).send(captor.capture());
        assertEquals("from@from.com", captor.getValue().getFrom()[0].toString());
        assertEquals(1, captor.getValue().getAllRecipients().length);
        assertEquals("to@to.com", captor.getValue().getAllRecipients()[0].toString());
        assertEquals("subject", captor.getValue().getSubject());
    }

}
