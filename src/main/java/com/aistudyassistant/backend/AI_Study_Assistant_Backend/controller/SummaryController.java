package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.SummaryDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping(value = "/{id}/summary", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SummaryDto> saveSummary(
            @PathVariable Long id,
            @RequestBody SummaryDto summaryDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        summaryDto.setNoteId(id);
        SummaryDto saved = summaryService.save(summaryDto, userDetails.getUsername());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}/summary", produces = "application/json")
    public ResponseEntity<SummaryDto> getSummaryByNoteId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SummaryDto summary = summaryService.getByNoteId(id, userDetails.getUsername());
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}
