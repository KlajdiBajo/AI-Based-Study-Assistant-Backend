package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizQuestion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizQuestionRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found or does not belong to user"));

        return quizQuestionRepository.findByQuiz(quiz).stream()
                .map(questionMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(Long quizId, List<QuizQuestionDto> questions, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> q.getNote().getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found or does not belong to user"));

        List<QuizQuestion> entities = questions.stream()
                .peek(this::validateQuizQuestion)
                .map(dto -> {
                    QuizQuestion q = questionMapper.mapFrom(dto);
                    q.setQuiz(quiz);
                    return q;
                })
                .collect(Collectors.toList());

        quizQuestionRepository.saveAll(entities);
    }

    // ==== Null byte validation helpers ====

    private boolean containsNullBytes(String text) {
        if (text == null) return false;
        return text.indexOf(0) != -1; // detects actual null byte (0x00)
    }

    private void validateQuizQuestion(QuizQuestionDto question) {
        List<String> fields = Arrays.asList(
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectAnswer()
        );

        for (String field : fields) {
            if (containsNullBytes(field)) {
                // IllegalArgumentException is correct here - it's a validation error, not a "not found" error
                throw new IllegalArgumentException("Null byte found in field: " +
                        field.replace("\u0000", "[NULL]"));
            }
        }
    }
}