package com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteDto {
    private Long noteId;

    private String fileName;

    private String title;

    private String fileURL;

    private String status;

    private LocalDateTime uploadedAt;

    private Long userId;
}
