package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Questions", description = "Quiz Question Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;

    @GetMapping(value = "/{quizId}/questions", produces = "application/json")
    @Operation(summary = "Get quiz questions", description = "Retrieve all questions for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = QuizQuestionDto.class)))),
            @ApiResponse(responseCode = "404", description = "Quiz not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<QuizQuestionDto>> getQuestions(
            @Parameter(description = "The ID of the quiz to get questions for", required = true)
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<QuizQuestionDto> questions = quizQuestionService.getQuestionsByQuizId(quizId, userDetails.getUsername());
        return ResponseEntity.ok(questions);
    }

    @PostMapping(value = "/{quizId}/questions", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Save quiz questions", description = "Save/update questions for a specific quiz (replaces existing questions)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Questions saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid question data or null bytes detected"),
            @ApiResponse(responseCode = "404", description = "Quiz not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> saveQuestions(
            @Parameter(description = "The ID of the quiz to save questions for", required = true)
            @PathVariable Long quizId,
            @Parameter(description = "List of questions to save", required = true)
            @RequestBody List<QuizQuestionDto> questions,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        quizQuestionService.saveAll(quizId, questions, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}