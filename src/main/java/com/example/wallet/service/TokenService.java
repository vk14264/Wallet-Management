package com.example.wallet.service;

import com.example.wallet.model.BlacklistedToken;
import com.example.wallet.model.RefreshToken;
import com.example.wallet.repository.BlacklistedTokenRepository;
import com.example.wallet.repository.RefreshTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Value("${security.jwt.expiration-ms}")
    private long refreshTokenValidityMs;

    @PostConstruct
    private void init() {
        refreshTokenValidityMs *= 24; // demo: refresh lasts longer
        log.info("TokenService initialized. Refresh validity: {} ms", refreshTokenValidityMs);
    }

    public RefreshToken createRefreshToken(String username) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(refreshTokenValidityMs);
        RefreshToken rt = new RefreshToken(username, token, expiry);
        log.debug("Created refresh token for {}: {}", username, token);
        return refreshTokenRepository.save(rt);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revoked: {}", token);
        });
    }

    public BlacklistedToken blacklistAccessToken(String token, Instant expiry) {
        BlacklistedToken b = new BlacklistedToken(token, expiry);
        log.info("Blacklisted access token: {}", token);
        return blacklistedTokenRepository.save(b);
    }

    public boolean isAccessTokenBlacklisted(String token) {
        boolean blacklisted = blacklistedTokenRepository.findByToken(token).isPresent();
        if (blacklisted) log.warn("Access token is blacklisted: {}", token);
        return blacklisted;
    }
}
