package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyStreakDto {

    private int currentStreak;

    private int longestStreak;

    private LocalDate streakStartDate;

    private LocalDate lastStudyDate;

    private List<LocalDate> studyDates; // for calendar visualization

    private boolean isActiveToday;
}