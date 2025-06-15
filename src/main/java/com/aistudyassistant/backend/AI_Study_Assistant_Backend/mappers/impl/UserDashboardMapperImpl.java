package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.UserDashboardDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDashboardMapperImpl implements Mapper<Object, UserDashboardDto> {

    private final ModelMapper modelMapper;

    @Override
    public UserDashboardDto mapTo(Object source) {
        // Dashboard DTOs are built programmatically in service layer
        return modelMapper.map(source, UserDashboardDto.class);
    }

    @Override
    public Object mapFrom(UserDashboardDto dto) {
        throw new UnsupportedOperationException("mapFrom not supported for UserDashboardDto");
    }
}