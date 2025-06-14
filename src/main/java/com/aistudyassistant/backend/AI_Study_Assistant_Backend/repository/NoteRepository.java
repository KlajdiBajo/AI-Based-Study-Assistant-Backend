package com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUser(User user);

    Optional<Note> findByIdAndUser(Long id, User user);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
            "(LOWER(n.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Note> findByUserAndFileNameOrTitleContainingIgnoreCase(
            @Param("user") User user,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    Page<Note> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);
}