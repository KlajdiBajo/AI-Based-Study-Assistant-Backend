// QuizAttemptController
package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAttemptDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizSubmissionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/submit")
    public ResponseEntity<QuizAttemptDto> submitQuiz(
            @RequestBody QuizSubmissionDto submission,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(quizAttemptService.submitQuiz(submission, userDetails.getUsername()));
    }

    @GetMapping("/my-attempts")
    public ResponseEntity<List<QuizAttemptDto>> getUserAttempts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(quizAttemptService.getUserAttempts(userDetails.getUsername()));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizAttemptDto>> getAttemptsByQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(quizAttemptService.getAttemptsByQuizId(quizId, userDetails.getUsername()));
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<QuizAttemptDto> getAttemptById(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return quizAttemptService.getAttemptById(attemptId, userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{attemptId}")
    public ResponseEntity<Void> deleteAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        quizAttemptService.deleteAttempt(attemptId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}