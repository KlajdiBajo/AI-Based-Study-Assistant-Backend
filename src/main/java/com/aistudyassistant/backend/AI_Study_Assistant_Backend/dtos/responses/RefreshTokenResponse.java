package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}
