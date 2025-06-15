package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotePerformanceDto {

    private Long noteId;

    private String noteTitle;

    private String fileName;

    private int totalAttempts;

    private double averageScore;

    private int bestScore;

    private double improvementRate;

    private LocalDateTime lastAttemptDate;

    private int totalQuestions;

    private int correctAnswers;

    private String performanceLevel; // "Excellent", "Good", "Needs Improvement"
}
