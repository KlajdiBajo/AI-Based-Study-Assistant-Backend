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
public class RecentActivityDto {

    private String activityType; // "QUIZ_COMPLETED", "QUIZ_CREATED", "NOTE_CREATED"

    private String description;

    private LocalDateTime timestamp;

    private String noteTitle;

    private Integer score; // null for non-quiz activities

    private Long relatedId; // quiz/note ID
}