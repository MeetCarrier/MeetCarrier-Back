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
    List<AssistantDataDto> assistantQuestions;
    List<AssistantDataDto> assistantAnswers;

    public static AssistantResponseDto fromEntity(List<AssistantQuestion> questions) {
        List<AssistantDataDto> questionDtos = new ArrayList<>();
        List<AssistantDataDto> answerDtos = new ArrayList<>();

        for (AssistantQuestion question : questions) {
            questionDtos.add(
                    AssistantDataDto.builder()
                            .content(question.getContent())
                            .createdAt(question.getSentAt())
                            .build()
            );

            AssistantAnswer answer = question.getAnswer();
            if (answer != null) {
                answerDtos.add(
                        AssistantDataDto.builder()
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
