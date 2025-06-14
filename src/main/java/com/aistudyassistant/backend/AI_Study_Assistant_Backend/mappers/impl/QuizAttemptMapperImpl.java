package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAttemptDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizAttemptMapperImpl implements Mapper<QuizAttempt, QuizAttemptDto> {

    private final ModelMapper modelMapper;
    private final Mapper<QuizAnswer, QuizAnswerDto> quizAnswerMapper;

    @Override
    public QuizAttemptDto mapTo(QuizAttempt quizAttempt) {
        // Configure ModelMapper to avoid conflicts
        configureModelMapper();

        // Use ModelMapper for basic fields only (score, attemptedAt)
        QuizAttemptDto quizAttemptDto = modelMapper.map(quizAttempt, QuizAttemptDto.class);

        // MANUAL mapping for ID fields to avoid conflicts
        quizAttemptDto.setQuizAttemptId(quizAttempt.getId());

        if(quizAttempt.getUser() != null) {
            quizAttemptDto.setUserId(quizAttempt.getUser().getId());
        }

        if(quizAttempt.getQuiz() != null) {
            quizAttemptDto.setQuizId(quizAttempt.getQuiz().getQuizId());

            if(quizAttempt.getQuiz().getNote() != null) {
                quizAttemptDto.setQuizTitle(quizAttempt.getQuiz().getNote().getTitle());
            }
        }

        if (quizAttempt.getAnswers() != null) {
            quizAttemptDto.setAnswers(quizAttempt.getAnswers().stream()
                    .map(quizAnswerMapper::mapTo)
                    .toList());
            quizAttemptDto.setTotalQuestions(quizAttempt.getAnswers().size());
        } else {
            quizAttemptDto.setTotalQuestions(0);
        }

        // Calculate percentage
        if (quizAttemptDto.getTotalQuestions() > 0) {
            quizAttemptDto.setPercentage((double) quizAttemptDto.getScore() / quizAttemptDto.getTotalQuestions() * 100);
        } else {
            quizAttemptDto.setPercentage(0.0);
        }

        return quizAttemptDto;
    }

    private void configureModelMapper() {
        // Set strict matching to avoid ambiguous mappings
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Create explicit mappings and skip problematic fields
        if (modelMapper.getTypeMap(QuizAttempt.class, QuizAttemptDto.class) == null) {
            modelMapper.createTypeMap(QuizAttempt.class, QuizAttemptDto.class)
                    .addMappings(mapper -> {
                        // Skip all ID-related fields - handle manually
                        mapper.skip(QuizAttemptDto::setQuizAttemptId);
                        mapper.skip(QuizAttemptDto::setQuizId);
                        mapper.skip(QuizAttemptDto::setUserId);
                        mapper.skip(QuizAttemptDto::setQuizTitle);
                        mapper.skip(QuizAttemptDto::setAnswers);
                        mapper.skip(QuizAttemptDto::setTotalQuestions);
                        mapper.skip(QuizAttemptDto::setPercentage);

                        // Only map simple fields automatically
                        mapper.map(QuizAttempt::getScore, QuizAttemptDto::setScore);
                        mapper.map(QuizAttempt::getAttemptedAt, QuizAttemptDto::setAttemptedAt);
                    });
        }
    }

    @Override
    public QuizAttempt mapFrom(QuizAttemptDto quizAttemptDto) {
        return modelMapper.map(quizAttemptDto, QuizAttempt.class);
    }
}