package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final Mapper<Note, NoteDto> noteMapper;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public NoteDto saveNote(MultipartFile file, NoteDto dto) throws IOException {
        // Save file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueName = UUID.randomUUID() + extension;

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(dir, uniqueName);
        file.transferTo(dest);

        // Update DTO
        dto.setFileName(originalFilename);
        dto.setFileURL(dest.getAbsolutePath());
        dto.setStatus("uploaded");
        dto.setUploadedAt(LocalDateTime.now());

        Note note = noteMapper.mapFrom(dto);
        Note saved = noteRepository.save(note);
        return noteMapper.mapTo(saved);
    }

    @Override
    public List<NoteDto> getNotesByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return noteRepository.findByUser(user).stream()
                .map(noteMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<NoteDto> getNoteById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return noteRepository.findById(id)
                .filter(note -> note.getUser().getId().equals(user.getId()))
                .map(noteMapper::mapTo);
    }

    @Override
    public void deleteNoteById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        noteRepository.findById(id)
                .filter(note -> note.getUser().getId().equals(user.getId()))
                .ifPresentOrElse(
                        noteRepository::delete,
                        () -> { throw new RuntimeException("Unauthorized or note not found"); }
                );
    }
}
