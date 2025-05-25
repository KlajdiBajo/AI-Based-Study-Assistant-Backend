package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

public interface OtpService {
    String generateOtp();
    String getOtpForEmail(String email);
}
