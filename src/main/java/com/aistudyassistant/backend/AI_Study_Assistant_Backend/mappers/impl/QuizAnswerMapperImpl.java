package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.QuizAnswerDto;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.QuizAnswer;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@RequiredArgsConstructor
public class QuizAnswerMapperImpl implements Mapper<QuizAnswer, QuizAnswerDto> {

    private final ModelMapper modelMapper;

    @Override
    public QuizAnswerDto mapTo(QuizAnswer quizAnswer) {
        QuizAnswerDto quizAnswerDto = modelMapper.map(quizAnswer, QuizAnswerDto.class);

        // FIXED: Null safety checks
        if (quizAnswer.getAttempt() != null) {
            quizAnswerDto.setAttemptId(quizAnswer.getAttempt().getId());
        }

        if (quizAnswer.getQuestion() != null) {
            // FIXED: Set questionId, not quizAnswerId (that was wrong)
            quizAnswerDto.setQuestionId(quizAnswer.getQuestion().getQuizQuestionid());
            quizAnswerDto.setQuestionText(quizAnswer.getQuestion().getQuestionText());

            // Set correct answer text based on correct option
            switch (quizAnswer.getQuestion().getCorrectOption()) {
                case 'A' -> quizAnswerDto.setCorrectAnswer(quizAnswer.getQuestion().getOptionA());
                case 'B' -> quizAnswerDto.setCorrectAnswer(quizAnswer.getQuestion().getOptionB());
                case 'C' -> quizAnswerDto.setCorrectAnswer(quizAnswer.getQuestion().getOptionC());
                case 'D' -> quizAnswerDto.setCorrectAnswer(quizAnswer.getQuestion().getOptionD());
                default -> quizAnswerDto.setCorrectAnswer("Unknown");
            }

            // Set selected answer text based on selected option
            switch (quizAnswer.getSelectedOption()) {
                case 'A' -> quizAnswerDto.setSelectedAnswer(quizAnswer.getQuestion().getOptionA());
                case 'B' -> quizAnswerDto.setSelectedAnswer(quizAnswer.getQuestion().getOptionB());
                case 'C' -> quizAnswerDto.setSelectedAnswer(quizAnswer.getQuestion().getOptionC());
                case 'D' -> quizAnswerDto.setSelectedAnswer(quizAnswer.getQuestion().getOptionD());
                default -> quizAnswerDto.setSelectedAnswer("Unknown");
            }
        }

        return quizAnswerDto;
    }

    @Override
    public QuizAnswer mapFrom(QuizAnswerDto quizAnswerDto) {
        return modelMapper.map(quizAnswerDto, QuizAnswer.class);
    }
}
