package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizQuestion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizQuestionRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizQuestionServiceImpl implements QuizQuestionService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizRepository quizRepository;
    private final Mapper<QuizQuestion, QuizQuestionDto> questionMapper;

    @Override
    public List<QuizQuestionDto> getQuestionsByQuizId(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> q.getNote().getUser().getEmail().equals(username))
                .orElseThrow(() -> new RuntimeException("Quiz not found or does not belong to user"));

        return quizQuestionRepository.findByQuiz(quiz).stream()
                .map(questionMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(Long quizId, List<QuizQuestionDto> questions, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> q.getNote().getUser().getEmail().equals(username))
                .orElseThrow(() -> new RuntimeException("Quiz not found or does not belong to user"));

        List<QuizQuestion> entities = questions.stream()
                .map(dto -> {
                    QuizQuestion q = questionMapper.mapFrom(dto);
                    q.setQuiz(quiz);
                    return q;
                })
                .collect(Collectors.toList());

        quizQuestionRepository.saveAll(entities);
    }
}

