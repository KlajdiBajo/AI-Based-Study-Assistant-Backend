package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface JwtService {
    RegisterVerifyResponse generateJwtTokenAndSetCookies(User user, HttpServletResponse response, HttpServletRequest request);

    ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response);

    String extractUsername(String token);
}
