package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Quiz;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Fixed: Find active quizzes by note (not by user directly)
    Optional<Quiz> findByNoteAndDeletedFalse(Note note);

    // Fixed: Find active quizzes for a user through note relationship
    @Query("SELECT q FROM Quiz q WHERE q.note.user = :user AND q.deleted = false")
    List<Quiz> findByNoteUserAndDeletedFalse(@Param("user") User user);

    // Keep this for reviews (includes deleted quizzes)
    Optional<Quiz> findByNote(Note note);

    // Optional: If you need all quizzes for a user (including deleted)
    @Query("SELECT q FROM Quiz q WHERE q.note.user = :user")
    List<Quiz> findByNoteUser(@Param("user") User user);
}