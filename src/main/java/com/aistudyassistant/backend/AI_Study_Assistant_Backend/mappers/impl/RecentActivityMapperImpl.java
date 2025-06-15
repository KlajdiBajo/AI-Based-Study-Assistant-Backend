package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.RecentActivityDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecentActivityMapperImpl implements Mapper<QuizAttempt, RecentActivityDto> {

    @Override
    public RecentActivityDto mapTo(QuizAttempt attempt) {
        String description = String.format("Completed quiz \"%s\" with score %d/%d",
                attempt.getQuiz().getNote().getTitle(),
                attempt.getScore(),
                attempt.getAnswers() != null ? attempt.getAnswers().size() : 0);

        return RecentActivityDto.builder()
                .activityType("QUIZ_COMPLETED")
                .description(description)
                .timestamp(attempt.getAttemptedAt())
                .noteTitle(attempt.getQuiz().getNote().getTitle())
                .score(attempt.getScore())
                .relatedId(attempt.getQuiz().getQuizId())
                .build();
    }

    @Override
    public QuizAttempt mapFrom(RecentActivityDto dto) {
        throw new UnsupportedOperationException("mapFrom not supported for RecentActivityDto");
    }
}