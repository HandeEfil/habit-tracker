package com.handegunaydin.habit_tracker.factory;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

@Component
public class SecurityEventFactory {

    public static SecurityEvent create(String mail, SecurityEventType eventType, Object source, Object target, Map<String, Object> metaData) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return new SecurityEvent(mail, SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, metaData);
        }
        String ip = requestAttributes.getAttribute("ip", RequestAttributes.SCOPE_REQUEST) != null ?
                (String) requestAttributes.getAttribute("ip", RequestAttributes.SCOPE_REQUEST) : null;
        String user_agent = requestAttributes.getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST) != null ?
                requestAttributes.getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST).toString() : null;
        return new SecurityEvent(mail, SecurityEventType.ACCOUNT_LOCKED, ip, user_agent, null, null, metaData);
    }


}
