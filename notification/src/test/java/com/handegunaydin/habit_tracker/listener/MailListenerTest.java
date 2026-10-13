package com.handegunaydin.habit_tracker.listener;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.EmailMessage;
import com.handegunaydin.habit_tracker.dto.UserRegisterEvent;
import com.handegunaydin.habit_tracker.service.EmailResolver;
import com.handegunaydin.habit_tracker.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailListenerTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private EmailResolver<DomainEvent> emailResolver;

    @InjectMocks
    private MailListener mailListener;

    @Test
    void mailSender_ShouldPassParametersCorrectly() {
        UserRegisterEvent event = mock(UserRegisterEvent.class);
        EmailMessage message = mock(EmailMessage.class);
        when(emailResolver.resolve(event)).thenReturn(message);
        doNothing().when(emailSender).send(message);
        mailListener.mailSender(event);
        verify(emailSender).send(message);

    }
}
