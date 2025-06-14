package com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_questionid")
    private Long quizQuestionId;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private char correctOption;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}
