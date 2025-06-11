package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByAttempt(QuizAttempt attempt);

    // FIXED: Custom query because field name is quizAnswerId, not id
    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.attempt.id = :attemptId")
    List<QuizAnswer> findByAttemptId(@Param("attemptId") Long attemptId);

}
