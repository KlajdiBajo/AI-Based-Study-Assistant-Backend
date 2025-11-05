package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.RefreshToken;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String token, HttpServletRequest request);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken refreshToken);

    void revokeToken(String token);

    void revokeAllUserTokens(User user);

    void deleteExpiredTokens();

}
