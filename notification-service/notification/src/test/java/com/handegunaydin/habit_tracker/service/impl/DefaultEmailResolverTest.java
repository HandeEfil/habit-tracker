package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.EmailMessage;
import com.handegunaydin.habit_tracker.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import com.handegunaydin.habit_tracker.service.EmailContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultEmailResolverTest {

    @Mock
    private EmailContextFactory<DomainEvent> emailContextFactory;

    @Mock
    private SpringTemplateEngine springTemplateEngine;
    @Mock
    private EmailContext<DomainEvent> emailContext;

    @InjectMocks
    private DefaultEmailResolver defaultEmailResolver;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(defaultEmailResolver, "mailSender", "dummy@test.com");
    }

    @Test
    void resolve_ifEmailContextResolverThrows_ShouldThrow() {
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = mock(UserFailedLoginAttemptEvent.class);
        when(emailContextFactory.getEmailContexts(userFailedLoginAttemptEvent)).thenThrow(NoSuchElementException.class);
        Assertions.assertThrows(NoSuchElementException.class, () -> defaultEmailResolver.resolve(userFailedLoginAttemptEvent));

    }

    @Test
    void resolve_emailContextShouldBeTriggeredWithFound() {
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = mock(UserFailedLoginAttemptEvent.class);
        when(emailContextFactory.getEmailContexts(userFailedLoginAttemptEvent)).thenReturn(emailContext);
        when(emailContext.resolveContext(userFailedLoginAttemptEvent)).thenReturn(new Context());
        defaultEmailResolver.resolve(userFailedLoginAttemptEvent);
        verify(emailContext, times(1)).resolveContext(userFailedLoginAttemptEvent);
    }


    @Test
    void resolve_shouldReturnEmailMessageWithCorrectFromToSubjectAndBody() {
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = new UserFailedLoginAttemptEvent("Test", "test@test.com", NotificationType.FAILED_LOGIN, UUID.randomUUID());
        when(emailContextFactory.getEmailContexts(userFailedLoginAttemptEvent)).thenReturn(emailContext);
        Context value = new Context();
        when(emailContext.resolveContext(userFailedLoginAttemptEvent)).thenReturn(value);
        when(emailContext.getTemplate()).thenReturn("email/body-template");
        when(emailContext.getSubject()).thenReturn("email/subject-template");
        when(springTemplateEngine.process("email/body-template", value)).thenReturn("processed-template");
        when(springTemplateEngine.process("email/subject-template", value)).thenReturn("processed-subject-template");
        EmailMessage resolve = defaultEmailResolver.resolve(userFailedLoginAttemptEvent);
        assertEquals("dummy@test.com", resolve.from());
        assertEquals("processed-template", resolve.body());
        assertEquals("processed-subject-template", resolve.subject());
    }


}
