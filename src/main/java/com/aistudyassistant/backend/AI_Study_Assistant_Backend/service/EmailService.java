package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

public interface EmailService {
    CompletableFuture<Integer> sendEmailWithRetry(String to, String otp) throws MessagingException, UnsupportedEncodingException;
    void sendOtpByEmail(String to, String otp) throws MessagingException, UnsupportedEncodingException;
}
