package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteMapperImpl implements Mapper<Note, NoteDto> {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

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