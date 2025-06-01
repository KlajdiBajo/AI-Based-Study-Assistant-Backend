package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizQuestionDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuizQuestionDto>> getQuestions(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(quizQuestionService.getQuestionsByQuizId(quizId, userDetails.getUsername()));
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<Void> saveQuestions(
            @PathVariable Long quizId,
            @RequestBody List<QuizQuestionDto> questions,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        quizQuestionService.saveAll(quizId, questions, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

