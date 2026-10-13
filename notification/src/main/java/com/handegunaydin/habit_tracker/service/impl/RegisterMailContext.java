package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.UserRegisterEvent;
import com.handegunaydin.habit_tracker.service.EmailContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
public class RegisterMailContext extends EmailContext {

    @Override
    public Context resolve(DomainEvent object) {
        UserRegisterEvent event = (UserRegisterEvent) object;
        Context context = new Context();
        context.setVariable("to", event.mail());
        context.setVariable("username", event.name());
        return context;
    }

    @Override
    public boolean supports(DomainEvent object) {
        return UserRegisterEvent.class.equals(object.getClass());
    }

    @Override
    public Object getType() {
        return UserRegisterEvent.class;
    }

    @Override
    public String getTemplate() {
        return "email/welcome-email-body";
    }

    @Override
    public String getSubject() {
        return "email/welcome-email-subject";
    }
}

