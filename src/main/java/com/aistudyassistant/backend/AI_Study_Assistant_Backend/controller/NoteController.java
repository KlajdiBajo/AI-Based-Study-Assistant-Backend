package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.NoteService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notes", description = "Note management APIs")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new note", description = "Upload a study note file (PDF, DOCX, TXT, DOC) for AI processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Note uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NoteDto.class))),
            @ApiResponse(responseCode = "400", description = """
                Bad Request:
                - File cannot be null or empty
                - File must have a valid name
                - File must have a valid extension
                - File type not supported. Allowed types: PDF, DOCX, TXT, DOC
                - Note data cannot be null
                - User ID is required
            """),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: User not found"),
            @ApiResponse(responseCode = "413", description = "Payload Too Large: File size exceeds maximum limit of 50MB"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Failed to save file or unexpected error")
    })
    public ResponseEntity<NoteDto> uploadNote(
            @Parameter(description = "The note file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        NoteDto dto = NoteDto.builder()
                .userId(user.getId())
                .build();

        NoteDto savedNote = noteService.saveNote(file, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
    }

    @GetMapping
    @Operation(summary = "Get user's notes", description = "Retrieve all notes belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NoteDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error retrieving user notes")
    })
    public ResponseEntity<List<NoteDto>> getUserNotes(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        List<NoteDto> notes = noteService.getNotesByUser(userDetails.getUsername());
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Get note by ID", description = "Retrieve a specific note by its ID (only if user owns it)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Note ID must be a positive number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: Note not found or access denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error retrieving note")
    })
    public ResponseEntity<Map<String, Object>> getNoteById(
            @Parameter(description = "The ID of the note to retrieve", required = true)
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        Optional<NoteDto> noteOpt = noteService.getNoteById(noteId, userDetails.getUsername());

        if (noteOpt.isPresent()) {
            NoteDto note = noteOpt.get();

            // Return format expected by Flask API
            Map<String, Object> response = new HashMap<>();
            response.put("noteId", note.getNoteId());
            response.put("title", note.getFileName());
            response.put("filePath", note.getFileURL());
            response.put("fileName", note.getFileName());
            response.put("uploadedAt", note.getUploadedAt());
            response.put("status", note.getStatus());
            response.put("userId", note.getUserId());

            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or access denied");
        }
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete note", description = "Delete a specific note by its ID (only if user owns it)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Note ID must be a positive number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: Note not found or access denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error deleting note")
    })
    public ResponseEntity<Map<String, String>> deleteNote(
            @Parameter(description = "The ID of the note to delete", required = true)
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        noteService.deleteNoteById(noteId, userDetails.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Note deleted successfully");
        response.put("noteId", noteId.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search user's notes with pagination",
            description = "Search notes by filename or title with pagination support. Results are sorted by upload date (newest first).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes search completed successfully"),
            @ApiResponse(responseCode = "400", description = """
            Bad Request:
            - Email cannot be null or empty
            - Page number cannot be negative
            - Page size must be between 1 and 100
        """),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Error searching notes")
    })
    public ResponseEntity<Map<String, Object>> searchNotes(
            @Parameter(description = "Search term to filter notes by filename or title")
            @RequestParam(value = "q", required = false) String searchTerm,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of items per page (1-100)")
            @RequestParam(value = "size", defaultValue = "10") int size,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        // Simplified: Only 3 parameters, fixed sorting
        Page<NoteDto> notePage = noteService.searchUserNotes(
                userDetails.getUsername(), searchTerm, page, size);

        // Create response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("notes", notePage.getContent());
        response.put("currentPage", notePage.getNumber());
        response.put("totalPages", notePage.getTotalPages());
        response.put("totalElements", notePage.getTotalElements());
        response.put("hasNext", notePage.hasNext());
        response.put("hasPrevious", notePage.hasPrevious());
        response.put("isFirst", notePage.isFirst());
        response.put("isLast", notePage.isLast());
        response.put("searchTerm", searchTerm);
        response.put("pageSize", size);
        response.put("sortedBy", "uploadedAt");  // Let frontend know the sort order
        response.put("sortDirection", "DESC");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{noteId}/process-ai")
    @Operation(summary = "Process note with AI", description = "Send note to AI service for summary and quiz generation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI processing completed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Note ID must be a positive number or JWT token invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or expired JWT token"),
            @ApiResponse(responseCode = "404", description = "Not Found: Note not found or access denied"),
            @ApiResponse(responseCode = "503", description = "Service Unavailable: AI service is currently unavailable"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Unexpected error during AI processing")
    })
    public ResponseEntity<Map<String, Object>> processNoteWithAI(
            @Parameter(description = "The ID of the note to process with AI", required = true)
            @PathVariable Long noteId,
            @Parameter(description = "JWT token for authentication", required = true)
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header is required");
        }

        noteService.processNoteWithAiModel(noteId, authHeader);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "AI content processed and saved successfully");
        response.put("noteId", noteId);
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }
}