package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyScoreDto {

    private LocalDate date;

    private double averageScore;

    private int attemptCount;

    private int totalQuestions;
}