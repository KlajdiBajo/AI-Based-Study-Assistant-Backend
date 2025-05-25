package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.http.ResponseEntity;

public interface JwtService {
    RegisterVerifyResponse generateJwtToken(User user);
    ResponseEntity<?> generateAccessTokenFromRefreshToken(String refreshToken);
}
