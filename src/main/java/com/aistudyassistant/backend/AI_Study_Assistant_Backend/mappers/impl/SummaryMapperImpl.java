package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.SummaryDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Summary;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SummaryMapperImpl implements Mapper<Summary, SummaryDto> {

    private final ModelMapper modelMapper;
    private final NoteRepository noteRepository;

    @PostConstruct
    public void init() {
        // Avoid ambiguity by explicitly creating a TypeMap and disabling implicit matching
        TypeMap<SummaryDto, Summary> typeMap = modelMapper.createTypeMap(SummaryDto.class, Summary.class);
        typeMap.addMappings(mapper -> {
            mapper.map(SummaryDto::getSummaryId, Summary::setId);
            mapper.skip(Summary::setNote);
        });
    }

    @Override
    public SummaryDto mapTo(Summary summary) {
        SummaryDto dto = modelMapper.map(summary, SummaryDto.class);
        if (summary.getNote() != null) {
            dto.setNoteId(summary.getNote().getId());
        }
        return dto;
    }

    @Override
    public Summary mapFrom(SummaryDto dto) {
        Summary summary = modelMapper.map(dto, Summary.class);
        summary.setNote(noteRepository.findById(dto.getNoteId())
                .orElseThrow(() -> new RuntimeException("Note not found")));
        return summary;
    }
}


