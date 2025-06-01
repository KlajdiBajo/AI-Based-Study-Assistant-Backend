package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.exceptions.ResourceNotFoundException;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final NoteRepository noteRepository;
    private final Mapper<Quiz, QuizDto> quizMapper;

    @Override
    public QuizDto save(QuizDto quizDto, String username) {
        Note note = noteRepository.findById(quizDto.getNoteId())
                .filter(n -> n.getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or does not belong to user"));

        Quiz quiz = quizMapper.mapFrom(quizDto);
        quiz.setNote(note);
        Quiz saved = quizRepository.save(quiz);
        return quizMapper.mapTo(saved);
    }

    @Override
    public QuizDto getByNoteId(Long noteId, String username) {
        Note note = noteRepository.findById(noteId)
                .filter(n -> n.getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or does not belong to user"));

        Quiz quiz = quizRepository.findByNote(note)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found for this note!"));

        return quizMapper.mapTo(quiz);
    }
}
