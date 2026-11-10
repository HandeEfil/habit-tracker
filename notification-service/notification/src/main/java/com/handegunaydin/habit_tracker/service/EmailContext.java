package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;

@Slf4j
public abstract class EmailContext<T extends DomainEvent> {
    public abstract Context resolve(T object);

    public abstract boolean supports(T object);

    public abstract String getTemplate();

    public abstract String getSubject();

    public abstract Object getType();


    public Context resolveContext(T object) {
        if (this.supports(object)) {
            return this.resolve(object);
        } else {
            log.error("the current type: {} is not supported for the channel : {}", object.getClass(), this.getClass());
            return null;
        }
    }


}
