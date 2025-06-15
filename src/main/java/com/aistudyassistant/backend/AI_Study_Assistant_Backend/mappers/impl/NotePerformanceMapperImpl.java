package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.NotePerformanceDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotePerformanceMapperImpl implements Mapper<Object, NotePerformanceDto> {

    private final ModelMapper modelMapper;

    @Override
    public NotePerformanceDto mapTo(Object source) {
        // This mapper is mainly for manual construction in service layer
        // since NotePerformanceDto is an aggregated data structure
        return modelMapper.map(source, NotePerformanceDto.class);
    }

    @Override
    public Object mapFrom(NotePerformanceDto dto) {
        // Not typically needed for dashboard DTOs
        throw new UnsupportedOperationException("mapFrom not supported for NotePerformanceDto");
    }
}