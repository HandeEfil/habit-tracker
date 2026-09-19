package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import com.handegunaydin.habit_tracker.auth.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.auth.service.TokenChainRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultTokenChainRevocationService implements TokenChainRevocationService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void detectRefreshTokenChain(RefreshToken oldRefreshToken) {
        if (oldRefreshToken.getReplacedByTokenId() == null) {
            if (!oldRefreshToken.isRevoked()) {
                oldRefreshToken.setRevoked(true);
                refreshTokenRepository.save(oldRefreshToken);
            }
            return;
        }
        Optional<RefreshToken> refreshTokenById = refreshTokenRepository.findRefreshTokenById(oldRefreshToken.getReplacedByTokenId());
        refreshTokenById.ifPresent(this::detectRefreshTokenChain);
    }


}
