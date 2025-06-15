package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.UserDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Dashboard", description = "User Progress Dashboard APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    @GetMapping(value = "/overview", produces = "application/json")
    @Operation(summary = "Get dashboard overview", description = "Get comprehensive dashboard with user's study statistics and progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDashboardDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserDashboardDto> getDashboardOverview(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserDashboardDto dashboard = userDashboardService.getDashboardOverview(userDetails.getUsername());
        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }

    @GetMapping(value = "/performance-trends", produces = "application/json")
    @Operation(summary = "Get performance trends", description = "Get performance trends over specified number of days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Performance trends retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PerformanceTrendDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PerformanceTrendDto> getPerformanceTrends(
            @Parameter(description = "Number of days to analyze (default: 30)")
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PerformanceTrendDto trends = userDashboardService.getScoreTrends(userDetails.getUsername(), days);
        return new ResponseEntity<>(trends, HttpStatus.OK);
    }

    @GetMapping(value = "/note-performance", produces = "application/json")
    @Operation(summary = "Get note performance", description = "Get performance breakdown by individual notes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note performance retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotePerformanceDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<NotePerformanceDto>> getNotePerformance(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<NotePerformanceDto> performance = userDashboardService.getNotePerformance(userDetails.getUsername());
        return new ResponseEntity<>(performance, HttpStatus.OK);
    }

    @GetMapping(value = "/weak-areas", produces = "application/json")
    @Operation(summary = "Get weak areas", description = "Get areas where user needs improvement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weak areas retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WeakAreaDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<WeakAreaDto>> getWeakAreas(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<WeakAreaDto> weakAreas = userDashboardService.getWeakAreas(userDetails.getUsername());
        return new ResponseEntity<>(weakAreas, HttpStatus.OK);
    }

    @GetMapping(value = "/study-streak", produces = "application/json")
    @Operation(summary = "Get study streak", description = "Get current study streak and streak history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Study streak retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StudyStreakDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StudyStreakDto> getStudyStreak(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StudyStreakDto streak = userDashboardService.getStudyStreak(userDetails.getUsername());
        return new ResponseEntity<>(streak, HttpStatus.OK);
    }

    @GetMapping(value = "/recommendations", produces = "application/json")
    @Operation(summary = "Get study recommendations", description = "Get personalized study recommendations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StudyRecommendationDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<StudyRecommendationDto>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<StudyRecommendationDto> recommendations = userDashboardService.getPersonalizedRecommendations(userDetails.getUsername());
        return new ResponseEntity<>(recommendations, HttpStatus.OK);
    }

    @GetMapping(value = "/recent-activities", produces = "application/json")
    @Operation(summary = "Get recent activities", description = "Get recent study activities and quiz attempts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RecentActivityDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<RecentActivityDto>> getRecentActivities(
            @Parameter(description = "Number of activities to return (default: 10)")
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<RecentActivityDto> activities = userDashboardService.getRecentActivities(userDetails.getUsername(), limit);
        return new ResponseEntity<>(activities, HttpStatus.OK);
    }
}