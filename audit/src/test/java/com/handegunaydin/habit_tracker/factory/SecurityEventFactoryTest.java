package com.handegunaydin.habit_tracker.factory;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SecurityEventFactoryTest {

    @Test
    void create_ShouldReturnEventWithProvidedEventType_WhenRequestAttributesExist() {

        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null);
        assertEquals(SecurityEventType.ACCOUNT_LOCKED, securityEvent.eventType());
    }

    @Test
    void create_ShouldSetIpAddress_WhenIpAttributeExists() {
        setIpAndAgent("127.0.0.1", "JUnit");
        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null);
        assertEquals("127.0.0.1", securityEvent.ipAddress());

    }


    @Test
    void create_ShouldSetUserAgent_WhenUserAgentAttributeExists() {
        setIpAndAgent("127.0.0.1", "JUnit");
        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null);
        assertEquals("JUnit", securityEvent.userAgent());

    }

    @Test
    void create_ShouldSetIpToNull_WhenIpAttributeIsMissing() {
        setIpAndAgent(null, null);
        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null);
        assertNull(securityEvent.ipAddress());

    }

    @Test
    void create_ShouldSetUserAgentToNull_WhenUserAgentAttributeIsMissing() {
        setIpAndAgent(null, null);
        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null);
        assertNull(securityEvent.userAgent());


    }

    @Test
    void create_ShouldPreserveMetadata_WhenMetadataIsProvided() {

        Map<String, Object> params = new HashMap<>();
        params.put("mock_key", "mock_value");
        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, params);
        assertEquals(1, securityEvent.metadata().size());
        assertEquals(params.get("mock_key"), securityEvent.metadata().get("mock_key"));
    }

    @Test
    void create_ShouldSetIpAndUserAgentToNull_WhenRequestAttributesAreMissing() {
        SecurityEvent securityEvent = SecurityEventFactory.create("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null);
        assertNull(securityEvent.ipAddress());
        assertNull(securityEvent.userAgent());

    }

    private static void setIpAndAgent(String ip, String agent) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("ip", ip);
        request.setAttribute("user-agent", agent);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }
}
