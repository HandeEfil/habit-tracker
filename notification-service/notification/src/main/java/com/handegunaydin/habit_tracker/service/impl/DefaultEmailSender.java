package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.EmailMessage;
import com.handegunaydin.habit_tracker.service.EmailSender;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultEmailSender implements EmailSender {
    private final JavaMailSender javaMailSender;

    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            mimeMessage.setFrom(emailMessage.from());
            mimeMessage.setSubject(emailMessage.subject());
            mimeMessage.setText(emailMessage.body(), "UTF-8", "html");
            mimeMessage.setRecipients(Message.RecipientType.TO, emailMessage.to());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
