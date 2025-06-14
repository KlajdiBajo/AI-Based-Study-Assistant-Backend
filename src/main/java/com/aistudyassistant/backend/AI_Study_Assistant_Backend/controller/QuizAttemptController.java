package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAttemptDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizSubmissionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAttemptService;
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
@RequestMapping("/quiz-attempts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Attempts", description = "Quiz Attempt Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/submit")
    @Operation(summary = "Submit a quiz attempt",
            description = "Submit answers for a quiz and get scored results. Users can retake quizzes unlimited times, but must wait 24 hours after deleting an attempt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz submitted successfully with calculated score",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuizAttemptDto.class))),
            @ApiResponse(responseCode = "400", description = """
                Bad Request:
                - Quiz submission cannot be null
                - Quiz ID must be a positive number
                - User answers cannot be null or empty
                - Question ID must be a positive number
                - No questions found for this quiz
                - Question not found or does not belong to this quiz
            """),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = """
                Not Found:
                - User not found
                - Quiz not found or does not belong to user
            """),
            @ApiResponse(responseCode = "409", description = "Conflict: You have already attempted this quiz recently. Please wait before retrying."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Unexpected error during quiz submission")
    })
    public ResponseEntity<QuizAttemptDto> submitQuiz(
            @Parameter(description = "Quiz submission data containing quiz ID and user answers", required = true)
            @RequestBody QuizSubmissionDto submission,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        QuizAttemptDto result = quizAttemptService.submitQuiz(submission, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-attempts")
    @Operation(summary = "Get user's quiz attempts",
            description = "Retrieve all quiz attempts made by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempts retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuizAttemptDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error retrieving quiz attempts")
    })
    public ResponseEntity<List<QuizAttemptDto>> getUserAttempts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        List<QuizAttemptDto> attempts = quizAttemptService.getUserAttempts(userDetails.getUsername());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get attempts for a specific quiz",
            description = "Retrieve all attempts made by the authenticated user for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempts retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuizAttemptDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Quiz ID must be a positive number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = """
                Not Found:
                - User not found
                - Quiz not found or does not belong to user
            """),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error retrieving quiz attempts")
    })
    public ResponseEntity<List<QuizAttemptDto>> getAttemptsByQuiz(
            @Parameter(description = "The ID of the quiz to get attempts for", required = true)
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        List<QuizAttemptDto> attempts = quizAttemptService.getAttemptsByQuizId(quizId, userDetails.getUsername());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get quiz attempt by ID",
            description = "Retrieve a specific quiz attempt by its ID (only if user owns it)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempt retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuizAttemptDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Attempt ID must be a positive number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = """
            Not Found:
            - User not found
            - Quiz attempt with specified ID not found or does not belong to this user
        """),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error retrieving quiz attempt")
    })
    public ResponseEntity<QuizAttemptDto> getAttemptById(
            @Parameter(description = "The ID of the quiz attempt to retrieve", required = true)
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        QuizAttemptDto attempt = quizAttemptService.getAttemptById(attemptId, userDetails.getUsername());
        return ResponseEntity.ok(attempt);
    }

    @DeleteMapping("/{attemptId}")
    @Operation(summary = "Delete quiz attempt",
            description = "Delete a specific quiz attempt by its ID (only if user owns it). Cannot delete attempts made within the last 5 minutes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempt deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Attempt ID must be a positive number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = """
                Not Found:
                - User not found
                - Quiz attempt not found or does not belong to user
            """),
            @ApiResponse(responseCode = "409", description = "Conflict: Cannot delete recent quiz attempts. Please wait 5 minutes after submission."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error deleting quiz attempt")
    })
    public ResponseEntity<Void> deleteAttempt(
            @Parameter(description = "The ID of the quiz attempt to delete", required = true)
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        quizAttemptService.deleteAttempt(attemptId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}