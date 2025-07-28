package com.ssipflow.backend.service;

import com.ssipflow.backend.entity.JwtBlacklist;
import com.ssipflow.backend.jwt.JwtUtil;
import com.ssipflow.backend.repository.JwtBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final JwtUtil jwtUtil;

    public void blacklistToken(String token, LocalDateTime expirationTime, String username) {
        JwtBlacklist jwtBlacklist = new JwtBlacklist();
        jwtBlacklist.setToken(token);
        jwtBlacklist.setExpirationTime(expirationTime);
        jwtBlacklist.setUsername(username);
        jwtBlacklistRepository.save(jwtBlacklist);
    }

    public boolean isTokenBlacklisted(String token) {
        String username = jwtUtil.getUsername(token);
        Optional<JwtBlacklist> blacklistedToken = jwtBlacklistRepository.findTopByUsernameOrderByExpirationTime(username);
        if (blacklistedToken.isEmpty()) {
            return false;
        }
        Instant instant = jwtUtil.getExpirationDate(token).toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return blacklistedToken.get().getExpirationTime().isAfter(localDateTime.minusMinutes(60));
    }
}
