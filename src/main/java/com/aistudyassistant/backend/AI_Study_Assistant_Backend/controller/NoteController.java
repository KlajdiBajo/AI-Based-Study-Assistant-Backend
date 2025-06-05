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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<NoteDto> noteOpt = noteService.getNoteById(noteId, userDetails.getUsername());

        if (noteOpt.isPresent()) {
            NoteDto note = noteOpt.get();

            // Return format expected by Flask API
            Map<String, Object> response = new HashMap<>();
            response.put("noteId", note.getNoteId());  // Your DTO uses 'id', not 'noteId'
            response.put("title", note.getFileName()); // Using fileName as title since you don't have a title field
            response.put("filePath", note.getFileURL());
            response.put("fileName", note.getFileName());
            response.put("uploadedAt", note.getUploadedAt());
            response.put("status", note.getStatus());
            response.put("userId", note.getUserId());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Note not found or access denied"));
        }
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        noteService.deleteNoteById(noteId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{noteId}/process-ai")
    public ResponseEntity<?> processNoteWithAI(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            noteService.processNoteWithAiModel(noteId, authHeader);
            return ResponseEntity.ok(Map.of("message", "AI content processed and saved successfully", "noteId", noteId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error during AI processing: " + e.getMessage()));
        }
    }

    // Test endpoint for debugging
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Note controller is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}