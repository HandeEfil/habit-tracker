package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    @Query("UPDATE RefreshToken r " +
            "set r.revoked = true where " +
            "r.tokenHashed = :hashedToken and r.revoked=false")
    int revokeIfActive(@Param(value = "hashedToken") String hashedToken);

    @Modifying
    @Query("UPDATE RefreshToken r " +
            "set r.revoked = true where " +
            "r.email = :email and r.revoked=false")
    void revokeAllForUser(@Param(value = "email") String email);

    Optional<RefreshToken> findRefreshTokenByTokenHashed(String hashedToken);

    Optional<RefreshToken> findRefreshTokenById(UUID id);

    List<RefreshToken> findRefreshTokenByEmail(String email);

}
