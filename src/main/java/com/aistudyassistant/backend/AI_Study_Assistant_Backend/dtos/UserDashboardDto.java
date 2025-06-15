package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

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
public class UserDashboardDto {

    // Basic Statistics
    private int totalQuizzesTaken;
    private double averageScore;
    private int bestScore;
    private int quizzesThisWeek;
    private double averageScoreThisWeek;

    // Study Progress
    private int currentStreak;
    private int totalStudyTimeMinutes;
    private LocalDateTime lastStudySession;

    // Performance Insights (by NOTE)
    private List<NotePerformanceDto> topPerformingNotes;
    private List<NotePerformanceDto> weakestNotes;
    private PerformanceTrendDto performanceTrend;

    // Recent Activity
    private List<RecentActivityDto> recentActivities;

    // Goals and Recommendations
    private List<StudyRecommendationDto> recommendations;
}