package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Summary;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    // For active operations (exclude deleted summaries)
    Optional<Summary> findByNoteAndDeletedFalse(Note note);

    // For internal operations and reviews (includes deleted summaries)
    Optional<Summary> findByNote(Note note);

    // Get all active summaries for a user (for UI display)
    @Query("SELECT s FROM Summary s WHERE s.note.user = :user AND s.deleted = false ORDER BY s.generatedAt DESC")
    List<Summary> findActiveByNoteUser(@Param("user") User user);
}