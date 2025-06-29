package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAttemptDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizSubmissionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.UserAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptServiceImpl.class);

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizAttemptDeletionRepository deletionRepository;
    private final Mapper<QuizAttempt, QuizAttemptDto> attemptMapper;


    @Override
    @Transactional
    public QuizAttemptDto submitQuiz(QuizSubmissionDto submission, String username) {
        // Input validation - using ResponseStatusException for consistency
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz submission cannot be null");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }
        if (submission.getQuizId() == null || submission.getQuizId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz ID must be a positive number");
        }
        if (submission.getUserAnswers() == null || submission.getUserAnswers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User answers cannot be null or empty");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Quiz quiz = quizRepository.findById(submission.getQuizId())
                .filter(q -> q.getNote().getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found or does not belong to user"));

        // Get all questions for this quiz
        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);

        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No questions found for this quiz");
        }

        // Validate user answers
        Set<Long> submittedQuestionIds = new HashSet<>();
        for (UserAnswerDto userAnswer : submission.getUserAnswers()) {
            if (userAnswer == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User answer cannot be null");
            }
            if (userAnswer.getQuestionId() == null || userAnswer.getQuestionId() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question ID must be a positive number");
            }
        }

        // Check for recent attempts OR recent deletions (24-hour rule)
        boolean hasRecentActivity = hasRecentQuizActivity(user, quiz);

        if (hasRecentActivity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have recently deleted an attempt for this quiz. Please wait 24 hours before retrying.");
        }

        // Create the attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .attemptedAt(LocalDateTime.now())
                .score(0) // Will be calculated below
                .totalQuestions(questions.size())
                .archived(false) // ADD THIS LINE - new attempts are always active
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Process answers and calculate score
        int correctAnswers = 0;
        int totalQuestions = questions.size();

        for (UserAnswerDto userAnswer : submission.getUserAnswers()) {
            QuizQuestion question = questions.stream()
                    .filter(q -> q.getQuizQuestionId().equals(userAnswer.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Question not found or does not belong to this quiz: " + userAnswer.getQuestionId()));

            // Compare user answer with correct answer (case-insensitive)
            char userSelectedOption = userAnswer.getSelectedOption();
            char correctOption = question.getCorrectOption();
            boolean isCorrect = Character.toUpperCase(userSelectedOption) == Character.toUpperCase(correctOption);

            if (isCorrect) {
                correctAnswers++;
            }

            QuizAnswer answer = QuizAnswer.builder()
                    .attempt(savedAttempt)
                    .question(question)
                    .selectedOption(userSelectedOption)
                    .correct(isCorrect)
                    .build();

            quizAnswerRepository.save(answer);
        }

        // Update the score
        savedAttempt.setScore(correctAnswers);
        QuizAttempt finalAttempt = quizAttemptRepository.save(savedAttempt);

        // Create the DTO with proper total questions and percentage
        QuizAttemptDto result = attemptMapper.mapTo(finalAttempt);
        result.setTotalQuestions(totalQuestions);
        result.setPercentage(totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0);

        return result;
    }

    @Override
    public List<QuizAttemptDto> getUserAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return quizAttemptRepository.findByUser(user).stream()
                .map(attemptMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizAttemptDto> getAttemptsByQuizId(Long quizId, String username) {
        if (quizId == null || quizId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz ID must be a positive number");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Quiz quiz = quizRepository.findById(quizId)
                .filter(q -> q.getNote().getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found or does not belong to user"));

        // Get attempts for current quiz
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserAndQuiz(user, quiz);

        // If no attempts found, try to find by filename (for re-uploaded files)
        if (attempts.isEmpty()) {
            String filename = quiz.getNote().getFileName();
            attempts = quizAttemptRepository.findByUserAndQuizNoteFileName(user, filename);
        }

        return attempts.stream()
                .map(attemptMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public QuizAttemptDto getAttemptById(Long attemptId, String username) {
        if (attemptId == null || attemptId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt ID must be a positive number");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        QuizAttempt attempt = quizAttemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz attempt with ID " + attemptId + " not found or does not belong to this user"));

        return attemptMapper.mapTo(attempt);
    }

    @Override
    @Transactional
    public void deleteAttempt(Long attemptId, String username) {
        if (attemptId == null || attemptId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt ID must be a positive number");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        QuizAttempt attempt = quizAttemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz attempt not found or does not belong to user"));

        // Business rule: prevent deletion of recent attempts
        if (attempt.getAttemptedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete recent quiz attempts. Please wait 5 minutes after submission.");
        }

        // IMPROVED: Record the deletion before deleting the attempt
        QuizAttemptDeletion deletion = QuizAttemptDeletion.builder()
                .user(user)
                .quiz(attempt.getQuiz())
                .originalAttemptDate(attempt.getAttemptedAt())
                .deletedAt(LocalDateTime.now())
                .originalScore(attempt.getScore())
                .deletionReason("User requested deletion")
                .build();

        deletionRepository.save(deletion);

        // Now delete the attempt
        quizAttemptRepository.delete(attempt);

        logger.info("Quiz attempt deleted and tracked. User: {}, Attempt ID: {}, Original Score: {}",
                username, attemptId, attempt.getScore());
    }

    @Override
    public List<Map<String, Object>> getUserAttemptsWithDocumentNames(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Long userId = user.getId();

        List<Object[]> results = quizAttemptRepository.findUserAttemptsWithDocumentNames(userId);
        return results.stream().map(row -> {
            Map<String, Object> attempt = new HashMap<>();
            attempt.put("quizAttemptId", row[0]);
            attempt.put("score", row[1]);
            attempt.put("attemptedAt", row[2]);
            attempt.put("quizId", row[3]);
            attempt.put("documentName", row[4]);
            attempt.put("totalQuestions", row[5]); // ADD THIS LINE
            return attempt;
        }).collect(Collectors.toList());
    }

    // IMPROVED: Check both attempts and deletions for 24-hour rule
    private boolean hasRecentQuizActivity(User user, Quiz quiz) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        // Check for recent deletions
        boolean hasRecentDeletion = deletionRepository.findByUserAndQuiz(user, quiz).stream()
                .anyMatch(deletion -> deletion.getDeletedAt().isAfter(twentyFourHoursAgo));

        return hasRecentDeletion;
    }
}