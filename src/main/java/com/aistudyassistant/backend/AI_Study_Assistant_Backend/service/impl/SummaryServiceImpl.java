package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.SummaryDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Summary;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.SummaryRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;
    private final NoteRepository noteRepository;
    private final Mapper<Summary, SummaryDto> summaryMapper;

    @Override
    public SummaryDto save(SummaryDto summaryDto, String username) {
        Note note = noteRepository.findById(summaryDto.getNoteId())
                .filter(n -> n.getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or does not belong to user"));

        Optional<Summary> existingSummary = summaryRepository.findByNote(note);

        Summary summary;
        if (existingSummary.isPresent()) {
            summary = existingSummary.get();
            summary.setContent(summaryDto.getContent());
            summary.setGeneratedAt(LocalDateTime.now());
        } else {
            summary = summaryMapper.mapFrom(summaryDto);
            summary.setNote(note);
            summary.setGeneratedAt(LocalDateTime.now());
        }

        Summary saved = summaryRepository.save(summary);
        return summaryMapper.mapTo(saved);
    }

    @Override
    public SummaryDto getByNoteId(Long noteId, String username) {
        Note note = noteRepository.findById(noteId)
                .filter(n -> n.getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or does not belong to user"));

        Summary summary = summaryRepository.findByNote(note)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Summary not found"));

        return summaryMapper.mapTo(summary);
    }
}