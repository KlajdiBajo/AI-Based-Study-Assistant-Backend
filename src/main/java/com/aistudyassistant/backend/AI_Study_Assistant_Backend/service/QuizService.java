package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizDto;

public interface QuizService {

    QuizDto save(QuizDto quizDto, String username);

    QuizDto getByNoteId(Long noteId, String username);
}
