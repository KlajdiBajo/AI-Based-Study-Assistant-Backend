package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NoteDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NoteMapperImpl implements Mapper<Note, NoteDto> {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public NoteMapperImpl(ModelMapper modelMapper, UserRepository userRepository) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }


    @Override
    public NoteDto mapTo(Note note) {
        return modelMapper.map(note, NoteDto.class);
    }

    @Override
    public Note mapFrom(NoteDto noteDto) {
        User user = userRepository.findById(noteDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Note note = modelMapper.map(noteDto, Note.class);
        note.setUser(user);
        return note;
    }
}
