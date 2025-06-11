package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUser(User user);
    List<QuizAttempt> findByQuiz(Quiz quiz);
    List<QuizAttempt> findByUserAndQuiz(User user, Quiz quiz);
    Optional<QuizAttempt> findByIdAndUser(Long id, User user);
}
