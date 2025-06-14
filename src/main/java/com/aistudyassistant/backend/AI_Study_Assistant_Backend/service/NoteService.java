package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.List;

public interface NoteService {

    NoteDto saveNote(MultipartFile file, NoteDto dto) throws IOException;

    List<NoteDto> getNotesByUser(String email);

    Optional<NoteDto> getNoteById(Long id, String email);

    void deleteNoteById(Long id, String email);

    void processNoteWithAiModel(Long noteId, String jwtToken);

    Page<NoteDto> searchUserNotes(String email, String searchTerm, int page, int size);
}
