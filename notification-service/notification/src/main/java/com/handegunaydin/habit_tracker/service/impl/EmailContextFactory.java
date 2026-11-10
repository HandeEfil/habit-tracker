package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.service.EmailContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EmailContextFactory<T extends DomainEvent> {
    private final Map<Object, EmailContext> emailContextsMap;

    public EmailContextFactory(List<EmailContext> emailContexts) {
        this.emailContextsMap = emailContexts.stream().collect(Collectors.toMap(EmailContext::getType, Function.identity()));
    }

    public EmailContext getEmailContexts(T object) {
        return emailContextsMap.get(object.getClass());
    }


}
