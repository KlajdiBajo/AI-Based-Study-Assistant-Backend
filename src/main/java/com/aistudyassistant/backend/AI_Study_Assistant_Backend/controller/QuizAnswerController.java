package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAnswerService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/quiz-answers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Answer Review", description = "APIs for reviewing detailed quiz answer breakdowns")
@SecurityRequirement(name = "bearerAuth")
public class QuizAnswerController {

    private final QuizAnswerService quizAnswerService;

    @GetMapping("/attempt/{attemptId}")
    @Operation(summary = "Review detailed quiz answers",
            description = "Get detailed breakdown of all answers for a completed quiz attempt, including correct answers and explanations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz answers retrieved successfully with detailed breakdown",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuizAnswerDto.class))),
            @ApiResponse(responseCode = "400", description = """
                Bad Request:
                - Attempt ID must be a positive number
                - Username cannot be null or empty
            """),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = """
                Not Found:
                - User not found
                - Quiz attempt not found or does not belong to user
            """),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error retrieving quiz answers")
    })
    public ResponseEntity<List<QuizAnswerDto>> getAnswersByAttempt(
            @Parameter(description = "The ID of the quiz attempt to review answers for", required = true)
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        List<QuizAnswerDto> answers = quizAnswerService.getAnswersByAttemptId(attemptId, userDetails.getUsername());
        return ResponseEntity.ok(answers);
    }
}