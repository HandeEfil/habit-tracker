package com.handegunaydin.habit_tracker.auth.controller;

import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import com.handegunaydin.habit_tracker.auth.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.auth.service.TokenGenerator;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenGenerator tokenGenerator;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${habit_tracker.max_failed_login_attempt.count}")
    private int maxFailedLoginAttempts;

    @Test
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        registerUser("not-an-email", "Test123!")
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn201_whenRegisterIsSuccessful() throws Exception {
        registerUser("test1234@test.com", "Test123!").andExpect(status().isCreated());
    }

    @Test
    void shouldReturn409_whenEmailAlreadyExists() throws Exception {
        registerUser("test@test.com", "Test123!");
        registerUser("test@test.com", "Test123!")
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200_whenLoginIsSuccessful() throws Exception {
        registerUser("test@test.com", "Test123!");
        loginUser("test@test.com", "Test123!")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401_whenLoginWithWrongPassword() throws Exception {
        registerUser("test@test.com", "Test123!");
        loginUser("test@test.com", "Test1234!")
                .andExpect(status().isUnauthorized());

    }

    @Test
    void shouldReturn401_whenLoginWithNonExistentUser() throws Exception {
        loginUser("test123@test123.com", "Test1234!")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_afterMaxFailedAttempts_returnsLockedEvenWithCorrectPassword() throws Exception {
        registerUser("blockedUser@block.com", "Test1234!");
        for (int i = 0; i < maxFailedLoginAttempts; i++) {
            loginUser("blockedUser@block.com", "Test12345!");
        }
        loginUser("blockedUser@block.com", "Test1234!").andExpect(status().isTooManyRequests());
    }

    @Test
    void login_whenCredentialsValid_shouldReturn200AndPersistRefreshToken() throws Exception {
        registerUser("test@test.com", "Test123!");
        loginUser("test@test.com", "Test123!")
                .andExpect(status().isOk()).andReturn();
        List<RefreshToken> tokenByTokenHashed = refreshTokenRepository.findRefreshTokenByEmail("test@test.com");
        assertFalse(tokenByTokenHashed.isEmpty());
    }

    @Test
    void login_whenCredentialsValid_shouldReturnRawTokenThatHashesToStoredValue() throws Exception {
        String rawToken = createUserAndGetRefreshToken();
        Optional<RefreshToken> tokenByTokenHashed = refreshTokenRepository.findRefreshTokenByTokenHashed(tokenGenerator.getTokenHash(rawToken));
        assertThat(tokenByTokenHashed).isPresent();
    }

    @Test
    void login_whenCredentialsInvalid_shouldReturn401AndNotPersistAnyToken() throws Exception {
        registerUser("test@test.com", "Test123!");
        loginUser("test@test.com", "Test1234!")
                .andExpect(status().isUnauthorized()).andReturn();
        List<RefreshToken> refreshTokenByEmail = refreshTokenRepository.findRefreshTokenByEmail("test@test.com");
        assertTrue(refreshTokenByEmail.isEmpty());

    }

    @Test
    void refreshEndpoint_whenRequestBodyMissingToken_shouldReturn400() throws Exception {
        String refreshJSON = """
                { }""";
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJSON)).andExpect(status().isBadRequest());
    }

    @Test
    void refreshEndpoint_whenRequestBodyBlank_shouldReturn400() throws Exception {
        refreshToken("").andExpect(status().isBadRequest());
    }

    @Test
    void refresh_whenTokenValidNotRevokedNotExpired_shouldReturn200() throws Exception {
        String rawToken = createUserAndGetRefreshToken();
        refreshToken(rawToken)
                .andExpect(status().isOk());
    }

    @Test
    void refresh_whenTokenValid_shouldPersistNewTokenAndRevokeOld() throws Exception {
        String rawOldToken = createUserAndGetRefreshToken();
        String newRefreshToken = extractRefreshToken(refreshToken(rawOldToken));

        Optional<RefreshToken> refreshToken = getOptionalTokenFromRawToken(rawOldToken);
        Optional<RefreshToken> refreshTokenNew = getOptionalTokenFromRawToken(newRefreshToken);

        assertTrue(refreshToken.isPresent());
        assertTrue(refreshToken.get().isRevoked());
        assertTrue(refreshTokenNew.isPresent());
    }


    @Test
    void refresh_whenTokenValid_shouldReturnNewRawTokenThatHashesToNewStoredValue() throws Exception {
        String rawOldToken = createUserAndGetRefreshToken();
        String newRefreshToken = extractRefreshToken(refreshToken(rawOldToken));
        Optional<RefreshToken> refreshTokenNew = getOptionalTokenFromRawToken(newRefreshToken);

        assertTrue(refreshTokenNew.isPresent());

    }

    @Test
    void refresh_whenTokenValid_shouldSetReplacedByTokenIdCorrectly() throws Exception {
        String rawOldToken = createUserAndGetRefreshToken();
        String newRefreshToken = extractRefreshToken(refreshToken(rawOldToken));
        Optional<RefreshToken> refreshToken = getOptionalTokenFromRawToken(rawOldToken);
        Optional<RefreshToken> refreshTokenNew = getOptionalTokenFromRawToken(newRefreshToken);
        assertTrue(refreshToken.isPresent());
        assertTrue(refreshToken.get().isRevoked());
        assertTrue(refreshTokenNew.isPresent());
        assertEquals(refreshToken.get().getReplacedByTokenId(), refreshTokenNew.get().getId());
    }

    private Optional<RefreshToken> getOptionalTokenFromRawToken(String newRefreshToken) {
        String tokenHashNew = tokenGenerator.getTokenHash(newRefreshToken);
        return refreshTokenRepository.findRefreshTokenByTokenHashed(tokenHashNew);
    }

    @Test
    void refresh_whenTokenExpired_shouldReturn401() throws Exception {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().minusMillis(90000L));
        refreshToken.setEmail("test@test.com");
        String rawToken = tokenGenerator.generateRawToken();
        String hashToken = tokenGenerator.getTokenHash(rawToken);
        refreshToken.setTokenHashed(hashToken);
        refreshTokenRepository.save(refreshToken);
        refreshToken(rawToken).andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_whenTokenExpired_shouldNotCreateNewToken() throws Exception {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().minusMillis(90000L));
        refreshToken.setEmail("test@test.com");
        String rawToken = tokenGenerator.generateRawToken();
        String hashToken = tokenGenerator.getTokenHash(rawToken);
        refreshToken.setTokenHashed(hashToken);
        refreshTokenRepository.save(refreshToken);
        long countBefore = refreshTokenRepository.count();
        refreshToken(rawToken).andExpect(status().isUnauthorized());
        long countAfter = refreshTokenRepository.count();
        assertEquals(countBefore, countAfter);

    }

    @Test
    void refresh_whenTokenExpired_shouldNotRevokeExpiredToken() throws Exception {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().minusMillis(90000L));
        refreshToken.setEmail("test@test.com");
        String rawToken = tokenGenerator.generateRawToken();
        String hashToken = tokenGenerator.getTokenHash(rawToken);
        refreshToken.setTokenHashed(hashToken);
        refreshTokenRepository.save(refreshToken);
        refreshToken(rawToken).andExpect(status().isUnauthorized());
        Optional<RefreshToken> tokenByTokenHashed = refreshTokenRepository.findRefreshTokenByTokenHashed(hashToken);
        assertTrue(tokenByTokenHashed.isPresent());
        assertFalse(tokenByTokenHashed.get().isRevoked());

    }

    @Test
    void refresh_whenTokenRevoked_shouldReturn401() throws Exception {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail("test@test.com");
        refreshToken.setExpiresAt(Instant.now().plusMillis(900000L));
        String rawToken = tokenGenerator.generateRawToken();
        String hashToken = tokenGenerator.getTokenHash(rawToken);
        refreshToken.setTokenHashed(hashToken);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        refreshToken(rawToken).andExpect(status().isUnauthorized());

    }

    @Test
    void refresh_whenTokenRevoked_shouldRevokeChainEndInDatabase() throws Exception {
        RefreshToken chainA = tokenCreator();
        assignTokenHash(chainA);

        RefreshToken chainB = tokenCreator();
        assignTokenHash(chainB);

        RefreshToken chainC = tokenCreator();
        String rawTokenC = assignTokenHash(chainC);

        refreshTokenRepository.save(chainA);
        refreshTokenRepository.save(chainB);
        refreshTokenRepository.save(chainC);
        chainB.setReplacedByTokenId(chainA.getId());
        chainC.setReplacedByTokenId(chainB.getId());
        chainB.setRevoked(true);
        chainC.setRevoked(true);

        refreshTokenRepository.save(chainB);
        refreshTokenRepository.save(chainC);
        refreshToken(rawTokenC).andExpect(status().isUnauthorized());
        Optional<RefreshToken> tokenByTokenHashed = refreshTokenRepository.findRefreshTokenByTokenHashed(chainA.getTokenHashed());
        assertTrue(tokenByTokenHashed.isPresent());
        assertTrue(tokenByTokenHashed.get().isRevoked());

    }


    private String createUserAndGetRefreshToken() throws Exception {
        registerUser("test@test.com", "Test123!");
        return extractRefreshToken(loginUser("test@test.com", "Test123!"));
    }

    private String extractRefreshToken(ResultActions rawOldToken) throws Exception {
        MvcResult mvcResultRefresh = rawOldToken
                .andExpect(status().isOk()).andReturn();
        String responseBodyRefresh = mvcResultRefresh.getResponse().getContentAsString();
        return JsonPath.read(responseBodyRefresh, "$.refreshToken");
    }

    private String assignTokenHash(RefreshToken refreshToken) {
        String rawTokenA = tokenGenerator.generateRawToken();
        String hashTokenA = tokenGenerator.getTokenHash(rawTokenA);
        refreshToken.setTokenHashed(hashTokenA);
        return rawTokenA;
    }


    private ResultActions refreshToken(String rawToken) throws Exception {
        String refreshJSON = """
                { "refreshToken": "%s"}""".formatted(rawToken);
        return mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJSON));
    }


    private ResultActions registerUser(String email, String password) throws Exception {
        String validJSON = """
                  { "name": "Test",
                "password": "%s",
                "mail": "%s",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""".formatted(password, email);
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJSON));
    }

    private ResultActions loginUser(String email, String password) throws Exception {
        String validJSON = """
                  {
                "password": "%s",
                "mail": "%s"
                  }""".formatted(password, email);
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJSON));
    }

    private RefreshToken tokenCreator() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setEmail("test@test.com");
        oldToken.setExpiresAt(Instant.now().plusMillis(900000L));

        return oldToken;
    }

}
