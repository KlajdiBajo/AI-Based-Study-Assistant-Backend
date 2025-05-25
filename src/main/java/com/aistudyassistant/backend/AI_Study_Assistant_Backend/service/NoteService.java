package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.List;

public interface NoteService {

    Note saveNote(MultipartFile file, User user) throws IOException;

    List<Note> getNotesByUser(User user);

    Optional<Note> getNoteById(long id);

    void deleteNoteById(long id);
}
