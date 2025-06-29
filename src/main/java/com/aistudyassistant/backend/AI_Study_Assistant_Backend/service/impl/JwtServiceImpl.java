package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.GeneralAPIResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RefreshTokenResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.security.JwtHelper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public RegisterVerifyResponse generateJwtToken(User user) {
        String access = jwtHelper.generateAccessToken(user);
        String refresh = jwtHelper.generateRefreshToken(user);
        return RegisterVerifyResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .firstName(user.getName().getFirstName())
                .lastName(user.getName().getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .build();
    }

    @Override
    public ResponseEntity<?> generateAccessTokenFromRefreshToken(String refreshToken) {
        System.out.println("🔄 Refresh token request received");
        System.out.println("🔍 Refresh token (first 20 chars): " + refreshToken.substring(0, 20) + "...");

        if(refreshToken != null) {
            try {
                String username = jwtHelper.extractUsername(refreshToken);
                System.out.println("📧 Extracted full subject: " + username);

                if(username.startsWith("#refresh")) {
                    String finalUserName = username.substring(8);
                    System.out.println("📧 Final username after substring: " + finalUserName);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(finalUserName);
                    System.out.println("👤 UserDetails loaded successfully for: " + finalUserName);

                    User user = userRepository.findByEmail(finalUserName).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + finalUserName)
                    );
                    System.out.println("🏠 User found in database: " + user.getEmail());

                    boolean isRefreshValid = jwtHelper.isRefreshTokenValid(refreshToken, userDetails);
                    System.out.println("✅ Is refresh token valid: " + isRefreshValid);

                    if(isRefreshValid) {
                        String accessToken = jwtHelper.generateAccessToken(user);
                        System.out.println("🎫 New access token generated successfully");
                        return new ResponseEntity<>(RefreshTokenResponse.builder()
                                .accessToken(accessToken)
                                .firstName(user.getName().getFirstName())
                                .lastName(user.getName().getLastName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build() , HttpStatus.OK);
                    } else {
                        System.out.println("❌ Refresh token validation failed");
                        return new ResponseEntity<>(GeneralAPIResponse.builder().message("Refresh token is expired").build() , HttpStatus.BAD_REQUEST);
                    }
                } else {
                    System.out.println("❌ Token doesn't start with #refresh");
                    return new ResponseEntity<>(GeneralAPIResponse.builder().message("Invalid refresh token").build() , HttpStatus.BAD_REQUEST);
                }
            } catch(Exception e) {
                System.out.println("💥 Exception occurred: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();

                if(e instanceof ExpiredJwtException) {
                    return new ResponseEntity<>(GeneralAPIResponse.builder().message("Refresh token is expired").build() , HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<>(GeneralAPIResponse.builder().message("Invalid refresh token").build() , HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            System.out.println("❌ Refresh token is null");
            return new ResponseEntity<>(GeneralAPIResponse.builder().message("Refresh token is null").build() , HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public String extractUsername(String token) {
        return jwtHelper.extractUsername(token);
    }
}