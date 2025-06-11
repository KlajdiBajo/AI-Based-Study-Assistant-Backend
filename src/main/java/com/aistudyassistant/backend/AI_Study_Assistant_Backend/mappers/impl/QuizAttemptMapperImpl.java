package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAttemptDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAttempt;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizAttemptMapperImpl implements Mapper<QuizAttempt, QuizAttemptDto> {

    private final ModelMapper modelMapper;
    private final Mapper<QuizAnswer, QuizAnswerDto> quizAnswerMapper;

    @Override
    public QuizAttemptDto mapTo(QuizAttempt quizAttempt) {
        QuizAttemptDto quizAttemptDto = modelMapper.map(quizAttempt, QuizAttemptDto.class);

        if(quizAttempt.getUser() != null) {
            quizAttemptDto.setUserId(quizAttempt.getUser().getId());
        }

        if(quizAttempt.getQuiz() != null) {
            quizAttemptDto.setQuizAttemptId(quizAttempt.getQuiz().getQuizId());

            if(quizAttempt.getQuiz().getNote() != null) {
                quizAttemptDto.setQuizTitle(quizAttempt.getQuiz().getNote().getTitle());
            }
        }

        if (quizAttempt.getAnswers() != null) {
            quizAttemptDto.setAnswers(quizAttempt.getAnswers().stream()
                    .map(quizAnswerMapper::mapTo)
                    .toList());
            quizAttemptDto.setTotalQuestions(quizAttempt.getAnswers().size());
        }

        // Calculate percentage
        if (quizAttemptDto.getTotalQuestions() > 0) {
            quizAttemptDto.setPercentage((double) quizAttemptDto.getScore() / quizAttemptDto.getTotalQuestions() * 100);
        }

        return quizAttemptDto;
    }

    @Override
    public QuizAttempt mapFrom(QuizAttemptDto quizAttemptDto) {
        return modelMapper.map(quizAttemptDto, QuizAttempt.class);
    }
}
