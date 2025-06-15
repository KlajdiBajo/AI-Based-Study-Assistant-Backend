package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyRecommendationDto {

    private String type; // "WEAK_NOTE", "PRACTICE_MORE", "NEW_TOPIC", "REVIEW"

    private String title;

    private String description;

    private String noteTitle;

    private String actionText; // "Retake Quiz", "Review Notes", etc.

    private String actionUrl;

    private int priority; // 1-5, higher is more important
}