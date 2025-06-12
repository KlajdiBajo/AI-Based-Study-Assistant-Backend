package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.requests;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "First name can't be blank")
    private String firstName;
    @NotBlank(message = "Last name can't be blank")
    private String lastName;
    @NotBlank(message = "Email can't be blank")
    @Email(message = "Invalid email entered")
    private String email;
    @NotBlank(message = "Password can't be blank")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character (@#$%^&+=!) and should be 8 characters long"
    )
    private String password;
    @NotNull(message ="Please choose your gender")
    private Gender gender;
    @Pattern(regexp = "^\\+\\d{1,3}\\d{10}$", message = "Invalid phone number, please enter in the format +(code)XXXXXXXXXX")
    private String phoneNumber;
}
