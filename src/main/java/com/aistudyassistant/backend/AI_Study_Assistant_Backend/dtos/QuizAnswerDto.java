package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAnswerDto {

    private Long quizAnswerId;

    private char selectedOption;

    private boolean correct;

    private Long attemptId;

    private Long questionId;

    private String questionText;
    private String correctAnswer;
    private String selectedAnswer;

}
