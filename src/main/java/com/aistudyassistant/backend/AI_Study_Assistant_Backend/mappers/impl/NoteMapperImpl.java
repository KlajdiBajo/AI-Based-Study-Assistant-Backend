package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteMapperImpl implements Mapper<Note, NoteDto> {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @PostConstruct
    public void configureMapper() {
        // Set strict matching strategy to avoid ambiguous mappings
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Configure DTO to Entity mapping (mapFrom)
        modelMapper.createTypeMap(NoteDto.class, Note.class)
                .addMapping(NoteDto::getNoteId, Note::setId)  // Explicitly map noteId to id
                .addMapping(NoteDto::getFileName, Note::setFileName)
                .addMapping(NoteDto::getFileURL, Note::setFileURL)
                .addMapping(NoteDto::getStatus, Note::setStatus)
                .addMapping(NoteDto::getUploadedAt, Note::setUploadedAt)
                .addMapping(NoteDto::getTitle, Note::setTitle)
                .addMapping(src -> null, Note::setUser); // We'll set user manually

        // Configure Entity to DTO mapping (mapTo)
        modelMapper.createTypeMap(Note.class, NoteDto.class)
                .addMapping(Note::getId, NoteDto::setNoteId)  // Explicitly map id to noteId
                .addMapping(Note::getFileName, NoteDto::setFileName)
                .addMapping(Note::getFileURL, NoteDto::setFileURL)
                .addMapping(Note::getStatus, NoteDto::setStatus)
                .addMapping(Note::getUploadedAt, NoteDto::setUploadedAt)
                .addMapping(Note::getTitle, NoteDto::setTitle)
                .addMapping(src -> null, NoteDto::setUserId); // We'll set userId manually
    }

    @Override
    public NoteDto mapTo(Note note) {
        NoteDto dto = modelMapper.map(note, NoteDto.class);

        if (note.getUser() != null) {
            dto.setUserId(note.getUser().getId());
        }

        return dto;
    }

    @Override
    public Note mapFrom(NoteDto noteDto) {
        Note note = modelMapper.map(noteDto, Note.class);

        // Set the User relationship
        if (noteDto.getUserId() != null) {
            User user = userRepository.findById(noteDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + noteDto.getUserId()));
            note.setUser(user);
        }

        return note;
    }
}