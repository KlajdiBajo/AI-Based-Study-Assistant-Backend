package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.GeneralAPIResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RefreshTokenResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.RefreshToken;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.security.JwtHelper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.JwtService;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.RefreshTokenService;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.utils.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Override
    @Transactional
    public RegisterVerifyResponse generateJwtTokenAndSetCookies(User user, HttpServletResponse response, HttpServletRequest request) {
        String accessToken = jwtHelper.generateAccessToken(user);
        String refreshToken = jwtHelper.generateRefreshToken(user);

        refreshTokenService.createRefreshToken(user, refreshToken, request);

        cookieUtil.addTokenCookies(response, accessToken, refreshToken);

        log.info("JWT tokens generated and set in cookies for user: {}", user.getEmail());

        return RegisterVerifyResponse.builder()
                .firstName(user.getName().getFirstName())
                .lastName(user.getName().getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refresh token request received");

        String refreshTokenValue = jwtHelper.getRefreshTokenFromCookies(request);
        log.info("Refresh token from cookie: {}", refreshTokenValue != null ? "Present" : "Missing");

        if(refreshTokenValue == null) {
            log.warn("No refresh token found in cookies");
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    log.info("Available cookie: {} = {}", cookie.getName(), cookie.getValue());
                }
            }
            return new ResponseEntity<>(
                    GeneralAPIResponse.builder().message("Refresh token not found").build(),
                    HttpStatus.UNAUTHORIZED
            );
        }

        try {
            String username = jwtHelper.extractUsername(refreshTokenValue);
            log.info("Extracted username from refresh token: {}", username);

            if (!username.startsWith("#refresh")) {
                log.warn("Invalid refresh token format");
                return new ResponseEntity<>(
                        GeneralAPIResponse.builder().message("Invalid refresh token").build(),
                        HttpStatus.BAD_REQUEST
                );
            }

            String finalUserName = username.substring(8);
            log.info("Final username after processing: {}", finalUserName);

            UserDetails userDetails = userDetailsService.loadUserByUsername(finalUserName);
            User user = userRepository.findByEmail(finalUserName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User not found with email " + finalUserName));

            RefreshToken dbRefreshToken = refreshTokenService.findByToken(refreshTokenValue)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Refresh token not found in database. Please login again."
                    ));
            refreshTokenService.verifyExpiration(dbRefreshToken);

            boolean isRefreshValid = jwtHelper.isRefreshTokenValid(refreshTokenValue, userDetails);
            if(!isRefreshValid) {
                log.warn("Refresh token JWT validation failed");
                return new ResponseEntity<>(
                        GeneralAPIResponse.builder().message("Refresh token is invalid").build(),
                        HttpStatus.BAD_REQUEST
                );
            }

            String newAccessToken = jwtHelper.generateAccessToken(user);

            response.addCookie(cookieUtil.createAccessTokenCookie(newAccessToken));
            log.info("New access token generated and set in cookie for user: {}", user.getEmail());

            return new ResponseEntity<>(
                    RefreshTokenResponse.builder()
                            .firstName(user.getName().getFirstName())
                            .lastName(user.getName().getLastName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .build(),
                    HttpStatus.OK
            );
        } catch (ExpiredJwtException e) {
            log.error("Refresh token has expired: {}", e.getMessage());

            refreshTokenService.revokeToken(refreshTokenValue);
            return new ResponseEntity<>(
                    GeneralAPIResponse.builder().message("Refresh Token has expired. Please login again.").build(),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    GeneralAPIResponse.builder().message("Invalid refresh token").build(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    @Override
    public String extractUsername(String token) {
        return jwtHelper.extractUsername(token);
    }
}