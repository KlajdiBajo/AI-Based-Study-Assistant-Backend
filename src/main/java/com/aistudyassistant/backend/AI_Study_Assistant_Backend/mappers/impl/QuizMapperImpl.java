package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizQuestion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuizMapperImpl implements Mapper<Quiz, QuizDto> {

    private final ModelMapper modelMapper;
    private final NoteRepository noteRepository;
    private final Mapper<QuizQuestion, QuizQuestionDto> quizQuestionMapper;

    @Override
    public QuizDto mapTo(Quiz quiz) {
        QuizDto dto = modelMapper.map(quiz, QuizDto.class);
        dto.setNoteId(quiz.getNote().getId());

        if (quiz.getQuestions() != null) {
            List<QuizQuestionDto> questionDtos = quiz.getQuestions().stream()
                    .map(quizQuestionMapper::mapTo)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDtos);
        }

        return dto;
    }

    @Override
    public Quiz mapFrom(QuizDto dto) {
        Quiz quiz = modelMapper.map(dto, Quiz.class);
        quiz.setNote(noteRepository.findById(dto.getNoteId())
                .orElseThrow(() -> new RuntimeException("Note not found")));

        if (dto.getQuestions() != null) {
            List<QuizQuestion> questions = dto.getQuestions().stream()
                    .map(quizQuestionMapper::mapFrom)
                    .peek(q -> q.setQuiz(quiz)) // set the owning side
                    .collect(Collectors.toList());
            quiz.setQuestions(questions);
        }

        return quiz;
    }
}