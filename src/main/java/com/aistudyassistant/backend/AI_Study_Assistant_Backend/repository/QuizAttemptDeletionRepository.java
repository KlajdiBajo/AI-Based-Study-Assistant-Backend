package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttemptDeletion;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptDeletionRepository extends JpaRepository<QuizAttemptDeletion, Long> {

    List<QuizAttemptDeletion> findByUserAndQuiz(User user, Quiz quiz);

    List<QuizAttemptDeletion> findByUser(User user);
}