package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;

import java.util.List;

public interface QuizAnswerService {

    List<QuizAnswerDto> getAnswersByAttemptId(Long attemptId, String username);
    QuizAnswerDto saveAnswer(QuizAnswerDto answerDto, String username);
    void saveAllAnswers(Long attemptId, List<QuizAnswerDto> answers, String username);

}
