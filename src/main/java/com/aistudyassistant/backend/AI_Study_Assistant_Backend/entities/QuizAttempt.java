package com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    private LocalDateTime attemptedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = true)
    private Quiz quiz;

    @Column(name = "document_name")
    private String documentName;

    // ADD THIS FIELD:
    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "archived", nullable = false)
    private Boolean archived = false;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizAnswer> answers;
}