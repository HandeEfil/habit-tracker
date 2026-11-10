package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.UserRegisterEvent;
import com.handegunaydin.habit_tracker.service.EmailContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailContextFactoryTest {

    @Mock
    private RegisterMailContext registerMailContext;


    @Test
    void getEmailContexts_shouldReturnContextMatchingEventType() {
        when(registerMailContext.getType()).thenReturn(UserRegisterEvent.class);
        EmailContextFactory<DomainEvent> emailContextFactory = new EmailContextFactory<>(List.of(registerMailContext));
        EmailContext<DomainEvent> filteredContext = emailContextFactory.getEmailContexts(mock(UserRegisterEvent.class));
        assertEquals(registerMailContext, filteredContext);


    }

    @Test
    void getEmailContexts_shouldReturnNullWhenNoContextRegistered() {
        EmailContextFactory<DomainEvent> emailContextFactory = new EmailContextFactory<>(Collections.emptyList());
        EmailContext<DomainEvent> filteredContext = emailContextFactory.getEmailContexts(mock(UserRegisterEvent.class));
        assertNull(filteredContext);

    }

    @Test
    void constructor_shouldFailWhenTwoContextsHandleSameType() { //write later

    }

}
