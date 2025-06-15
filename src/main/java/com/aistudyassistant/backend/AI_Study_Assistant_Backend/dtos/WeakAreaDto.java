package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeakAreaDto {

    private String fileName;

    private int mistakeCount;

    private double errorRate;

    private String recommendedAction;

    private int questionsAttempted;

    private int correctAnswers;
}