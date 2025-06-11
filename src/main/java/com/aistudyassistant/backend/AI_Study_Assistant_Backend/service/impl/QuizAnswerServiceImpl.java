package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizQuestion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizAnswerRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizAttemptRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizQuestionRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAnswerServiceImpl implements QuizAnswerService {

    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserRepository userRepository;
    private final Mapper<QuizAnswer, QuizAnswerDto> answerMapper;

    @Override
    public List<QuizAnswerDto> getAnswersByAttemptId(Long attemptId, String username) {
        // Use the repository method that includes user validation
        QuizAttempt attempt = findAttemptByIdAndUsername(attemptId, username);

        return quizAnswerRepository.findByAttempt(attempt).stream()
                .map(answerMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public QuizAnswerDto saveAnswer(QuizAnswerDto answerDto, String username) {
        // Validate that the attempt belongs to the user
        QuizAttempt attempt = findAttemptByIdAndUsername(answerDto.getAttemptId(), username);

        QuizQuestion question = quizQuestionRepository.findById(answerDto.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));

        // Validate that the question belongs to the same quiz as the attempt
        if (!question.getQuiz().getQuizId().equals(attempt.getQuiz().getQuizId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Question does not belong to the quiz associated with this attempt");
        }

        // Check for duplicate answer (if business logic requires preventing duplicates)
        boolean answerExists = quizAnswerRepository.findByAttempt(attempt).stream()
                .anyMatch(answer -> answer.getQuestion().getQuizQuestionid().equals(question.getQuizQuestionid()));

        if (answerExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Answer already exists for this question in this attempt");
        }

        QuizAnswer answer = answerMapper.mapFrom(answerDto);
        answer.setAttempt(attempt);
        answer.setQuestion(question);

        // Determine if the answer is correct
        answer.setCorrect(answer.getSelectedOption() == question.getCorrectOption());

        QuizAnswer saved = quizAnswerRepository.save(answer);

        return answerMapper.mapTo(saved);
    }

    @Override
    public void saveAllAnswers(Long attemptId, List<QuizAnswerDto> answers, String username) {
        QuizAttempt attempt = findAttemptByIdAndUsername(attemptId, username);

        // Check for duplicate questions in the submission
        Set<Long> questionIds = new HashSet<>();
        for (QuizAnswerDto dto : answers) {
            if (!questionIds.add(dto.getQuestionId())) {
                throw new IllegalArgumentException("Duplicate question found in submission: " + dto.getQuestionId());
            }
        }

        List<QuizAnswer> answerEntities = answers.stream()
                .map(dto -> {
                    QuizQuestion question = quizQuestionRepository.findById(dto.getQuestionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found: " + dto.getQuestionId()));

                    // Validate that the question belongs to the same quiz as the attempt
                    if (!question.getQuiz().getQuizId().equals(attempt.getQuiz().getQuizId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Question does not belong to the quiz associated with this attempt");
                    }

                    QuizAnswer answer = answerMapper.mapFrom(dto);
                    answer.setAttempt(attempt);
                    answer.setQuestion(question);
                    answer.setCorrect(answer.getSelectedOption() == question.getCorrectOption());

                    return answer;
                })
                .collect(Collectors.toList());

        quizAnswerRepository.saveAll(answerEntities);
    }

    // Helper method to reduce code duplication
    private QuizAttempt findAttemptByIdAndUsername(Long attemptId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + username));

        return quizAttemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz attempt not found or does not belong to user"));
    }
}