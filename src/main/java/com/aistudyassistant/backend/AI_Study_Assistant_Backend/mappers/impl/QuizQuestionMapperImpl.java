package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizQuestion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizQuestionMapperImpl implements Mapper<QuizQuestion, QuizQuestionDto> {

    private final ModelMapper modelMapper;

    @Override
    public QuizQuestionDto mapTo(QuizQuestion quizQuestionEntity) {
        QuizQuestionDto dto = modelMapper.map(quizQuestionEntity, QuizQuestionDto.class);

        // Set correctAnswer manually based on correctOption
        switch (quizQuestionEntity.getCorrectOption()) {
            case 'A' -> dto.setCorrectAnswer(quizQuestionEntity.getOptionA());
            case 'B' -> dto.setCorrectAnswer(quizQuestionEntity.getOptionB());
            case 'C' -> dto.setCorrectAnswer(quizQuestionEntity.getOptionC());
            case 'D' -> dto.setCorrectAnswer(quizQuestionEntity.getOptionD());
            default -> dto.setCorrectAnswer("Unknown");
        }

        return dto;
    }

    @Override
    public QuizQuestion mapFrom(QuizQuestionDto dto) {
        QuizQuestion quizQuestionEntity = modelMapper.map(dto, QuizQuestion.class);
        return quizQuestionEntity;
    }
}