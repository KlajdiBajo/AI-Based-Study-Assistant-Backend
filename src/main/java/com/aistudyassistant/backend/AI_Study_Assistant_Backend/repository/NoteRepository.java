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

    // For active operations (exclude deleted notes) - UI will use these
    List<Note> findByUserAndDeletedFalse(User user);
    Optional<Note> findByIdAndUserAndDeletedFalse(Long id, User user);
    Page<Note> findByUserAndDeletedFalseOrderByUploadedAtDesc(User user, Pageable pageable);

    // For internal operations (includes deleted notes) - keep for referential integrity
    List<Note> findByUser(User user);
    Optional<Note> findById(Long id);
    Optional<Note> findByIdAndUser(Long id, User user);

    // Updated search query to exclude deleted notes
    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.deleted = false AND " +
            "(LOWER(n.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Note> findByUserAndFileNameOrTitleContainingIgnoreCase(
            @Param("user") User user,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // Keep the old method name but make it active-only
    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.deleted = false ORDER BY n.uploadedAt DESC")
    Page<Note> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);
}