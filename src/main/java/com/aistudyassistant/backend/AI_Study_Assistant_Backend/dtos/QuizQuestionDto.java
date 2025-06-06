package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizQuestionDto {
    private Long quizQuestionId;

    private String questionText;

    private String correctAnswer;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private char correctOption;
}

