package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.EmailMessage;
import com.handegunaydin.habit_tracker.service.EmailContext;
import com.handegunaydin.habit_tracker.service.EmailResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class DefaultEmailResolver implements EmailResolver<DomainEvent> {

    private final EmailContextFactory<DomainEvent> emailContextFactory;
    private final SpringTemplateEngine springTemplateEngine;
    @Value("${spring.mail.username}")
    private String mailSender;

    @Override
    public EmailMessage resolve(DomainEvent event) {
        EmailContext<DomainEvent> emailContextResolver = emailContextFactory.getEmailContexts(event);// consider if throws !!
        Context context = emailContextResolver.resolveContext(event);
        return new EmailMessage(mailSender, (String) context.getVariable("to"),
                springTemplateEngine.process(emailContextResolver.getSubject(), context),
                springTemplateEngine.process(emailContextResolver.getTemplate(), context));

    }
}
