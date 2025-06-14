package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizAnswerRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.QuizAttemptRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAnswerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAnswerServiceImpl implements QuizAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(QuizAnswerServiceImpl.class);

    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final Mapper<QuizAnswer, QuizAnswerDto> answerMapper;

    @Override
    public List<QuizAnswerDto> getAnswersByAttemptId(Long attemptId, String username) {
        if (attemptId == null || attemptId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt ID must be a positive number");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }

        // Find the attempt and validate user ownership
        QuizAttempt attempt = findAttemptByIdAndUsername(attemptId, username);

        List<QuizAnswer> answers = quizAnswerRepository.findByAttempt(attempt);

        logger.info("Retrieved {} answers for attempt {}. User: {}",
                answers.size(), attemptId, username);

        return answers.stream()
                .map(answerMapper::mapTo)
                .collect(Collectors.toList());
    }

    // Helper method for validation and security
    private QuizAttempt findAttemptByIdAndUsername(Long attemptId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return quizAttemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz attempt not found or does not belong to user"));
    }
}