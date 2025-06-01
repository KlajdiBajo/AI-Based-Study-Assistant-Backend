package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizMapperImpl implements Mapper<Quiz, QuizDto> {

    private final ModelMapper modelMapper;
    private final NoteRepository noteRepository;

    @PostConstruct
    public void init() {
        TypeMap<QuizDto, Quiz> typeMap = modelMapper.createTypeMap(QuizDto.class, Quiz.class);
        typeMap.addMappings(mapper -> {
            mapper.map(QuizDto::getQuizId, Quiz::setId);
            mapper.skip(Quiz::setNote); // Set manually later
        });
    }

    @Override
    public QuizDto mapTo(Quiz quiz) {
        QuizDto dto = modelMapper.map(quiz, QuizDto.class);
        dto.setNoteId(quiz.getNote().getId());
        return dto;
    }

    @Override
    public Quiz mapFrom(QuizDto dto) {
        Quiz quiz = modelMapper.map(dto, Quiz.class);
        quiz.setNote(noteRepository.findById(dto.getNoteId())
                .orElseThrow(() -> new RuntimeException("Note not found")));
        return quiz;
    }
}


