package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.SummaryDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Summary;

public interface SummaryService {

    SummaryDto save(SummaryDto summaryDto, String username);

    SummaryDto getByNoteId(Long noteId, String username);

}
