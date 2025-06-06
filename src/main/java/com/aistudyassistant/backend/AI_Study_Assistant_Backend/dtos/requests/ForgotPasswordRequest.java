package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForgotPasswordRequest {
    @Email(message = "enter a valid email")
    @NotBlank(message = "Email can't be blank")
    private String email;
}
