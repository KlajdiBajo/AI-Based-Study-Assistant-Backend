package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizQuestion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizQuestionMapperImpl implements Mapper<QuizQuestion, QuizQuestionDto> {

    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        TypeMap<QuizQuestionDto, QuizQuestion> typeMap = modelMapper.createTypeMap(QuizQuestionDto.class, QuizQuestion.class);
        typeMap.addMappings(mapper -> {
            mapper.map(QuizQuestionDto::getQuizId, QuizQuestion::setId);
            mapper.skip(QuizQuestion::setQuiz); // Manually handled if needed
        });
    }

    @Override
    public QuizQuestionDto mapTo(QuizQuestion quizQuestion) {
        return modelMapper.map(quizQuestion, QuizQuestionDto.class);
    }

    @Override
    public QuizQuestion mapFrom(QuizQuestionDto dto) {
        return modelMapper.map(dto, QuizQuestion.class);
    }
}


