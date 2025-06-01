package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;


import java.util.List;

public interface QuizQuestionService {

    List<QuizQuestionDto> getQuestionsByQuizId(Long quizId, String username);

    void saveAll(Long quizId, List<QuizQuestionDto> questions, String username);
}
