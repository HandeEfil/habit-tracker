package com.handegunaydin.habit_tracker.jwt;

import com.handegunaydin.habit_tracker.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {

    @Value("${jwt.expiration}")
    private long expirationTime;

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secretKey) {

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, List<Role> roles) {
        Map<String, List<String>> roleMap = new HashMap<>();
        roleMap.put("roles", convertRolesToString(roles));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey)
                .claims(roleMap)
                .compact();
    }

    private List<String> convertRolesToString(List<Role> roles) {
        return roles != null ? roles.stream().map(Role::name).toList(): Collections.emptyList();
    }

    public String extractUserName(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public List<String> extractRoles(String token) {
        return (List<String>) Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload().get("roles");

    }

    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    protected boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUserName = extractUserName(token);
            return extractedUserName.equals(username) && !isTokenExpired(token);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            return false;
        }
    }


}
