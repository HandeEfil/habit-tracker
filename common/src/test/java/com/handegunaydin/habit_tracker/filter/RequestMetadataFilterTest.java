package com.handegunaydin.habit_tracker.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
public class RequestMetadataFilterTest {

    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private RequestMetadataFilter requestMetadataFilter;

    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();

        ServletRequestAttributes attributes = new ServletRequestAttributes(mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void shouldSetRequestMetadata_WhenRequestIsReceived() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        mockHttpServletRequest.setAttribute("ip", "127.0.0.1");
        mockHttpServletRequest.addHeader("user-agent", "mock");
        doAnswer(invocationOnMock -> {
            assertEquals("127.0.0.1", RequestContextHolder.getRequestAttributes().getAttribute("ip", RequestAttributes.SCOPE_REQUEST));
            assertEquals("mock", RequestContextHolder.getRequestAttributes().getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST));
            return null;
        }).when(filterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        requestMetadataFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain);

    }

    @Test
    void shouldRemoveRequestMetadata_AfterFilterChainCompletes() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        mockHttpServletRequest.setAttribute("ip", "127.0.0.1");
        mockHttpServletRequest.addHeader("user-agent", "mock");
        doAnswer(invocationOnMock -> {
            assertEquals("127.0.0.1", RequestContextHolder.getRequestAttributes().getAttribute("ip", RequestAttributes.SCOPE_REQUEST));
            assertEquals("mock", RequestContextHolder.getRequestAttributes().getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST));
            return null;
        }).when(filterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        requestMetadataFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain);
        assertNull(RequestContextHolder.getRequestAttributes().getAttribute("ip", RequestAttributes.SCOPE_REQUEST));
        assertNull(RequestContextHolder.getRequestAttributes().getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST));

    }

    @Test
    void shouldRemoveRequestMetadata_WhenFilterChainThrowsException() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        mockHttpServletRequest.setAttribute("ip", "127.0.0.1");
        mockHttpServletRequest.addHeader("user-agent", "mock");
        doAnswer(invocationOnMock -> {
            assertEquals("127.0.0.1", RequestContextHolder.getRequestAttributes().getAttribute("ip", RequestAttributes.SCOPE_REQUEST));
            assertEquals("mock", RequestContextHolder.getRequestAttributes().getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST));
            throw new NullPointerException();
        }).when(filterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        assertThrows(NullPointerException.class, () -> requestMetadataFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain));
        assertNull(RequestContextHolder.getRequestAttributes().getAttribute("ip", RequestAttributes.SCOPE_REQUEST));
        assertNull(RequestContextHolder.getRequestAttributes().getAttribute("user-agent", RequestAttributes.SCOPE_REQUEST));

    }
}
