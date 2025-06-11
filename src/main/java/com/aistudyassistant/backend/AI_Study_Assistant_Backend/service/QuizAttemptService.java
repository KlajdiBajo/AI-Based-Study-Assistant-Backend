package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAttemptDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizSubmissionDto;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptService {

    QuizAttemptDto submitQuiz(QuizSubmissionDto submission, String username);
    List<QuizAttemptDto> getUserAttempts(String username);
    List<QuizAttemptDto> getAttemptsByQuizId(Long quizId, String username);
    Optional<QuizAttemptDto> getAttemptById(Long attemptId, String username);
    void deleteAttempt(Long attemptId, String username);

}
