package com.kslj.mannam.domain.assistant.dto;

import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantResponseDto {
    List<AssistantDto> assistantQuestions;
    List<AssistantDto> assistantAnswers;

    public static AssistantResponseDto fromEntity(List<AssistantQuestion> questions) {
        List<AssistantDto> questionDtos = new ArrayList<>();
        List<AssistantDto> answerDtos = new ArrayList<>();

        for (AssistantQuestion question : questions) {
            questionDtos.add(
                    AssistantDto.builder()
                            .content(question.getContent())
                            .createdAt(question.getSentAt())
                            .build()
            );

            AssistantAnswer answer = question.getAnswer();
            if (answer != null) {
                answerDtos.add(
                        AssistantDto.builder()
                                .content(answer.getContent())
                                .createdAt(answer.getSentAt())
                                .build()
                );
            }
        }

        return AssistantResponseDto.builder()
                .assistantQuestions(questionDtos)
                .assistantAnswers(answerDtos)
                .build();
    }
}
