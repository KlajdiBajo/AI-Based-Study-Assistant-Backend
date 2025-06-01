package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<NoteDto> uploadNote(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        NoteDto dto = NoteDto.builder()
                .userId(user.getId())
                .build();

        NoteDto savedNote = noteService.saveNote(file, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
    }

    @GetMapping
    public ResponseEntity<List<NoteDto>> getUserNotes(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<NoteDto> notes = noteService.getNotesByUser(userDetails.getUsername());
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNoteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return noteService.getNoteById(id, userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        noteService.deleteNoteById(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notes/{id}/process-ai")
    public ResponseEntity<?> processNoteWithAI(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            noteService.processNoteWithAiModel(id, authHeader);
            return ResponseEntity.ok("AI content processed and saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during AI processing: " + e.getMessage());
        }
    }
}

