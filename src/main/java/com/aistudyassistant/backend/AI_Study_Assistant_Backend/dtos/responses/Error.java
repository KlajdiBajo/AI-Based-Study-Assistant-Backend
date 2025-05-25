package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Error {
    private String errorCode;
    private String errorMessage;
    private String additionalInfo;
}
