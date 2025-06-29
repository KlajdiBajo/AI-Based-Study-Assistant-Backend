package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.SummaryDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Note;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Summary;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.NoteRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.SummaryRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final Mapper<Summary, SummaryDto> summaryMapper;

    @Override
    public SummaryDto save(SummaryDto summaryDto, String username) {
        Note note = noteRepository.findById(summaryDto.getNoteId())
                .filter(n -> n.getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or does not belong to user"));

        // CHANGED: Only check for active (non-deleted) summaries
        Optional<Summary> existingSummary = summaryRepository.findByNoteAndDeletedFalse(note);

        Summary summary;
        if (existingSummary.isPresent()) {
            // Update existing active summary
            summary = existingSummary.get();
            String cleanedContent = summaryDto.getContent()
                    .replaceAll("(?i)SUMMARY:\\s*-?\\s*", "")
                    .replaceAll("(?m)^-\\s+", "");
            summary.setContent(cleanedContent);
            summary.setGeneratedAt(LocalDateTime.now());
        } else {
            // Create new summary (ignores any soft-deleted ones)
            summary = summaryMapper.mapFrom(summaryDto);
            summary.setNote(note);
            summary.setDeleted(false); // Ensure new summary is active

            // Clean the content first
            String cleanedContent = summaryDto.getContent()
                    .replaceAll("(?i)SUMMARY:\\s*-?\\s*", "")
                    .replaceAll("(?m)^-\\s+", "");
            summary.setContent(cleanedContent);
            summary.setGeneratedAt(LocalDateTime.now());
        }

        Summary saved = summaryRepository.save(summary);
        return summaryMapper.mapTo(saved);
    }

    @Override
    public SummaryDto getByNoteId(Long noteId, String username) {
        Note note = noteRepository.findById(noteId)
                .filter(n -> n.getUser().getEmail().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or access denied"));

        // CHANGED: Only get active (non-deleted) summaries
        Summary summary = summaryRepository.findByNoteAndDeletedFalse(note)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No summary found for this note"));

        return summaryMapper.mapTo(summary);
    }

    // ADD: Method to get all active summaries for a user (for UI listing)
    public List<SummaryDto> getUserSummaries(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return summaryRepository.findActiveByNoteUser(user)
                .stream()
                .map(summaryMapper::mapTo)
                .collect(Collectors.toList());
    }
}