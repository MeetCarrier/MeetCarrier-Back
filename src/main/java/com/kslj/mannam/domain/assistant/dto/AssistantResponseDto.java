package com.kslj.mannam.domain.assistant.dto;

import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantResponseDto {
    List<AssistantQuestion> assistantQuestions;
    List<AssistantAnswer> assistantAnswers;
}
