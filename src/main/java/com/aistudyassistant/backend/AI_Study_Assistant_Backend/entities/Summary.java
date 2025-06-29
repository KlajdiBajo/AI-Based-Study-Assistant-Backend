package com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "summaries")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long summaryId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime generatedAt;

    // ADD THESE FIELDS FOR SOFT DELETE:
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "document_name")
    private String documentName; // Store original filename

    @OneToOne
    @JoinColumn(name = "note_id")
    private Note note;
}