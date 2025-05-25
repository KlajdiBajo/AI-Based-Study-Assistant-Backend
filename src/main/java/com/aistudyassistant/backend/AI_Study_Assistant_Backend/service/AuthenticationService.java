package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.requests.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    ResponseEntity<RegisterResponse> registerUser(RegisterRequest registerRequest);
    ResponseEntity<?> verifyUserRegistration(RegisterVerifyRequest registerVerifyRequest);
    ResponseEntity<?> loginUser(LoginRequest loginRequest);
    ResponseEntity<?> resendOtp(ForgotPasswordRequest forgotPasswordRequest);
    ResponseEntity<?> verifyOtp(RegisterVerifyRequest registerVerifyRequest);
    ResponseEntity<?> resetPassword(ResetPasswordRequest resetPasswordRequest);
    ResponseEntity<?> myProfile(ForgotPasswordRequest forgotPasswordRequest);
}
