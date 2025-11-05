package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.requests.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    ResponseEntity<RegisterResponse> registerUser(RegisterRequest registerRequest);

    ResponseEntity<?> verifyUserRegistration(RegisterVerifyRequest registerVerifyRequest, HttpServletResponse response, HttpServletRequest request);

    ResponseEntity<?> loginUser(LoginRequest loginRequest, HttpServletResponse response, HttpServletRequest request);

    ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<?> resendOtp(ForgotPasswordRequest forgotPasswordRequest);

    ResponseEntity<?> verifyOtp(RegisterVerifyRequest registerVerifyRequest);

    ResponseEntity<?> resetPassword(ResetPasswordRequest resetPasswordRequest);

    ResponseEntity<?> myProfile(String authenticatedEmail);
}
