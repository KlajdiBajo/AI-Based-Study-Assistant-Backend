package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformanceTrendDto {

    private String trendDirection; // "IMPROVING", "STABLE", "DECLINING"

    private double trendPercentage;

    private List<DailyScoreDto> dailyScores;

    private String summary; // Human-readable trend description
}