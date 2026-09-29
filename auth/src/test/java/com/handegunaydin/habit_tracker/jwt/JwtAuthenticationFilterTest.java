package com.handegunaydin.habit_tracker.jwt;

import com.handegunaydin.habit_tracker.enums.Role;
import com.handegunaydin.habit_tracker.service.RedisTokenService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @Mock
    private RedisTokenService redisTokenService;

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
        assertNull(SecurityContextHolder.getContext().getAuthentication());

    }

    @Test
    void shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        when(jwtService.isTokenValid("test@test.com_token", "test@test.com")).thenReturn(true);
        when(jwtService.extractUserName("test@test.com_token")).thenReturn("test@test.com");
        when(jwtService.extractID("test@test.com_token")).thenReturn(UUID.randomUUID().toString());
        when(jwtService.extractIssuedTime("test@test.com_token")).thenReturn(Date.from(Instant.now().minusSeconds(600)));
        when(jwtService.extractRoles("test@test.com_token")).thenReturn(List.of(Role.CUSTOMER.toString()));
        when(redisTokenService.isAccessTokenDisabled(any())).thenReturn(false);
        when(redisTokenService.isAllAccessTokensForUserDisabled(any(), any())).thenReturn(false);
        jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("test@test.com", authentication.getName());
        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);

    }

    @Test
    void shouldNotCrash_whenTokenIsExpired() throws ServletException, IOException {
        when(jwtService.extractUserName("test@test.com_token")).thenThrow(ExpiredJwtException.class);
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain));
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    void shouldNotCrash_whenAccessTokenDisabled() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        when(jwtService.extractUserName("test@test.com_token")).thenReturn("test@test.com");
        when(jwtService.extractID("test@test.com_token")).thenReturn(UUID.randomUUID().toString());
        when(jwtService.extractIssuedTime("test@test.com_token")).thenReturn(Date.from(Instant.now().minusSeconds(600)));
        when(jwtService.extractRoles("test@test.com_token")).thenReturn(List.of(Role.CUSTOMER.toString()));
        when(redisTokenService.isAccessTokenDisabled(any())).thenReturn(true);
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain));
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

    }

    @Test
    void shouldNotCrash_whenAllAccessTokenForUserDisabled() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        when(jwtService.extractUserName("test@test.com_token")).thenReturn("test@test.com");
        when(jwtService.extractID("test@test.com_token")).thenReturn(UUID.randomUUID().toString());
        when(jwtService.extractIssuedTime("test@test.com_token")).thenReturn(Date.from(Instant.now().minusSeconds(600)));
        when(jwtService.extractRoles("test@test.com_token")).thenReturn(List.of(Role.CUSTOMER.toString()));
        when(redisTokenService.isAccessTokenDisabled(any())).thenReturn(false);
        when(redisTokenService.isAllAccessTokensForUserDisabled(any(), any())).thenReturn(true);
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain));
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    void shouldNotSetAuthentication_whenTokenIsInvalid() throws ServletException, IOException {
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        when(jwtService.isTokenValid("test@test.com_token", "test@test.com")).thenReturn(false);
        when(jwtService.extractUserName("test@test.com_token")).thenReturn("test@test.com");
        when(jwtService.extractID("test@test.com_token")).thenReturn(UUID.randomUUID().toString());
        when(jwtService.extractIssuedTime("test@test.com_token")).thenReturn(Date.from(Instant.now().minusSeconds(600)));
        when(jwtService.extractRoles("test@test.com_token")).thenReturn(List.of(Role.CUSTOMER.toString()));
        when(redisTokenService.isAccessTokenDisabled(any())).thenReturn(false);
        when(redisTokenService.isAllAccessTokensForUserDisabled(any(), any())).thenReturn(false);
        mockHttpServletRequest.addHeader("Authorization", "Bearer test@test.com_token");
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, filterChain));
        verify(filterChain, times(1)).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

    }
}
