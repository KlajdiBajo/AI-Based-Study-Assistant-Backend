package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByAttempt(QuizAttempt attempt);

    // FIXED: Custom query because field name is quizAnswerId, not id
    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.attempt.id = :attemptId")
    List<QuizAnswer> findByAttemptId(@Param("attemptId") Long attemptId);

    // ========== NEW METHODS FOR DASHBOARD ==========
    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.attempt.user = :user")
    List<QuizAnswer> findByAttemptUser(@Param("user") User user);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.attempt.user = :user AND qa.correct = false")
    List<QuizAnswer> findByAttemptUserAndCorrectFalse(@Param("user") User user);

    @Query("SELECT COUNT(qa) FROM QuizAnswer qa WHERE qa.attempt.user = :user AND qa.correct = true")
    long countCorrectAnswersByUser(@Param("user") User user);

    @Query("SELECT COUNT(qa) FROM QuizAnswer qa WHERE qa.attempt.user = :user AND qa.correct = false")
    long countIncorrectAnswersByUser(@Param("user") User user);
}