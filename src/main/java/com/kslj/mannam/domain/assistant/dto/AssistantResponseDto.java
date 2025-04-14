package com.kslj.mannam.domain.assistant.dto;

import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import lombok.Data;

import java.util.List;

@Data
public class AssistantResponseDto {
    List<AssistantQuestion> assistantQuestions;
    List<AssistantAnswer> assistantAnswers;
}
