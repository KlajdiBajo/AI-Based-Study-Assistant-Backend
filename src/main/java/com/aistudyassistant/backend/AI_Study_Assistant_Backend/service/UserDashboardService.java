package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.*;

import java.util.List;

public interface UserDashboardService {

    /**
     * Get comprehensive dashboard overview for a user
     * @param username User's email/username
     * @return Dashboard data with all metrics
     */
    UserDashboardDto getDashboardOverview(String username);

    /**
     * Get score trends over specified number of days
     * @param username User's email/username
     * @param days Number of days to analyze (default: 30)
     * @return Score trends with daily breakdown
     */
    PerformanceTrendDto getScoreTrends(String username, int days);

    /**
     * Get performance breakdown by note
     * @param username User's email/username
     * @return List of note performances sorted by average score
     */
    List<NotePerformanceDto> getNotePerformance(String username);

    /**
     * Get areas where user needs improvement
     * @param username User's email/username
     * @return List of weak areas with recommendations
     */
    List<WeakAreaDto> getWeakAreas(String username);

    /**
     * Get study streak information
     * @param username User's email/username
     * @return Current and historical streak data
     */
    StudyStreakDto getStudyStreak(String username);

    /**
     * Get personalized study recommendations
     * @param username User's email/username
     * @return List of recommendations prioritized by importance
     */
    List<StudyRecommendationDto> getPersonalizedRecommendations(String username);

    /**
     * Get recent user activities
     * @param username User's email/username
     * @param limit Number of activities to return (default: 10)
     * @return List of recent activities
     */
    List<RecentActivityDto> getRecentActivities(String username, int limit);
}