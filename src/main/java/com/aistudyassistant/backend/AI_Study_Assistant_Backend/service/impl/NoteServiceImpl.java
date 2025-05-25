package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
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

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public Note saveNote(MultipartFile file, User user) throws IOException {
        //Save file to disk
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;
        File dir = new File(UPLOAD_DIR);

        if(!dir.exists()) dir.mkdirs();
        File dest = new File(dir, uniqueFileName);
        file.transferTo(dest);

        //Create Note record
        Note note = Note.builder()
                .fileName(file.getOriginalFilename())
                .fileURL(UPLOAD_DIR + uniqueFileName)
                .status("uploaded")
                .uploadedAt(LocalDateTime.now())
                .user(user)
                .build();

        return noteRepository.save(note);
    }

    @Override
    public List<Note> getNotesByUser(User user) {
        return noteRepository.findByUser(user);
    }

    @Override
    public Optional<Note> getNoteById(long id) {
        return noteRepository.findById(id);
    }

    @Override
    public void deleteNoteById(long id) {
        noteRepository.deleteById(id);
    }
}
