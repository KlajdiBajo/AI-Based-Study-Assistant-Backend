package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/{id}/quiz")
    public ResponseEntity<QuizDto> saveQuiz(
            @PathVariable Long id,
            @RequestBody QuizDto quizDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        quizDto.setNoteId(id);
        QuizDto result = quizService.save(quizDto, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/quiz")
    public ResponseEntity<QuizDto> getQuizByNoteId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        QuizDto quizDto = quizService.getByNoteId(id, userDetails.getUsername());
        return new ResponseEntity<>(quizDto, HttpStatus.OK);
    }
}

