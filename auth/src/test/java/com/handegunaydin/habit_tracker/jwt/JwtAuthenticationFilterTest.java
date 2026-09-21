package com.handegunaydin.habit_tracker.jwt;

import io.jsonwebtoken.ExpiredJwtException;
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
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotSetAuthentication_whenNoAuthHeaderPresent() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);

    }

    @Test
    void shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
        when(jwtService.isTokenValid("test@test.com_token", "test@test.com")).thenReturn(true);
        when(jwtService.extractUserName("test@test.com_token")).thenReturn("test@test.com");
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain);
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test
    void shouldNotCrash_whenTokenIsExpired() throws ServletException, IOException {
        when(jwtService.extractUserName("test@test.com_token")).thenThrow(ExpiredJwtException.class);
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain));
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);
    }
}
