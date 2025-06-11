package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAttemptDto {

    private Long quizAttemptId;

    private int score;

    private LocalDateTime attemptedAt;

    private Long userId;

    private Long quizId;

    private List<QuizAnswerDto> answers;

    // Additional fields for easier frontend usage
    private int totalQuestions;
    private double percentage;
    private String quizTitle;

}
