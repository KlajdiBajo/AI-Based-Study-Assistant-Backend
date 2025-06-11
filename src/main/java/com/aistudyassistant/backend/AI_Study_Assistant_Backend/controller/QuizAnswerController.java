// QuizAnswerController
package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz-answers")
@RequiredArgsConstructor
public class QuizAnswerController {

    private final QuizAnswerService quizAnswerService;

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<List<QuizAnswerDto>> getAnswersByAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(quizAnswerService.getAnswersByAttemptId(attemptId, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<QuizAnswerDto> saveAnswer(
            @RequestBody QuizAnswerDto answerDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(quizAnswerService.saveAnswer(answerDto, userDetails.getUsername()));
    }

    @PostMapping("/attempt/{attemptId}")
    public ResponseEntity<Void> saveAllAnswers(
            @PathVariable Long attemptId,
            @RequestBody List<QuizAnswerDto> answers,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        quizAnswerService.saveAllAnswers(attemptId, answers, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}