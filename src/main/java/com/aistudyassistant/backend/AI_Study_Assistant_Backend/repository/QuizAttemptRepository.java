package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUser(User user);

    List<QuizAttempt> findByQuiz(Quiz quiz);

    List<QuizAttempt> findByUserAndQuiz(User user, Quiz quiz);

    Optional<QuizAttempt> findByIdAndUser(Long id, User user);

    List<QuizAttempt> findByUserAndAttemptedAtAfter(User user, LocalDateTime after);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user = :user AND qa.attemptedAt >= :after ORDER BY qa.attemptedAt ASC")
    List<QuizAttempt> findByUserAndAttemptedAtAfterOrderByAttemptedAt(@Param("user") User user, @Param("after") LocalDateTime after);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user = :user ORDER BY qa.attemptedAt DESC")
    List<QuizAttempt> findByUserOrderByAttemptedAtDesc(@Param("user") User user);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.user = :user AND qa.attemptedAt >= :startDate")
    long countByUserAndAttemptedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.user = :user AND qa.attemptedAt >= :startDate")
    Double findAverageScoreByUserAndAttemptedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
}