package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.enums.Gender;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Gender gender;
    private Role role;
    private String profilePicture;
    private Boolean isOfficiallyEnabled;
}
