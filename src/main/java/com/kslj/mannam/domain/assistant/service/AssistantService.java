package com.kslj.mannam.domain.assistant.service;

import com.kslj.mannam.domain.assistant.dto.AssistantDataDto;
import com.kslj.mannam.domain.assistant.dto.AssistantResponseDto;
import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import com.kslj.mannam.domain.assistant.repository.AssistantAnswerRepository;
import com.kslj.mannam.domain.assistant.repository.AssistantQuestionRepository;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssistantService {

    private final AssistantAnswerRepository assistantAnswerRepository;
    private final AssistantQuestionRepository assistantQuestionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    // 질문 등록 및 AI 비서로 전달
    @Transactional
    public AssistantQuestion createQuestionAndSendToAI(User user, String questionContent) {
        // 질문 저장
        AssistantQuestion newQuestion = AssistantQuestion.builder()
                .content(questionContent)
                .user(user)
                .build();
        AssistantQuestion savedQuestion = assistantQuestionRepository.save(newQuestion);

        // 메시지 구성 및 전송
        Map<String, Object> message = new HashMap<>();
        message.put("userId", user.getId());
        message.put("questionId", savedQuestion.getId());
        message.put("question", savedQuestion.getContent());

        rabbitTemplate.convertAndSend("ai_request_queue", message);

        return savedQuestion;
    }

    // AI 비서 답변 수신 및 등록
    @Transactional
    @RabbitListener(queues = "ai_response_queue")
    public void createAnswer(Map<String, Object> response) {
        try {
            long userId = ((Number) response.get("userId")).longValue();
            long questionId = ((Number) response.get("questionId")).longValue();
            String answer = (String) response.get("answer");

            AssistantQuestion question = assistantQuestionRepository.findById(questionId)
                    .orElseThrow(() -> new EntityNotFoundException("질문을 찾을 수 없습니다. questionId=" + questionId));

            AssistantAnswer newAnswer = AssistantAnswer.builder()
                    .content(answer)
                    .question(question)
                    .build();

            AssistantAnswer savedAnswer = assistantAnswerRepository.save(newAnswer);

            AssistantDataDto answerDto = AssistantDataDto.builder()
                    .content(answer)
                    .createdAt(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/assistant/" + userId, answerDto);

        } catch (Exception e) {
            log.error("Failed to process AI response", e);
        }
    }

    // 질문 삭제
    @Transactional
    public long delete(long questionId) {
        assistantQuestionRepository.deleteById(questionId);

        return questionId;
    }

    // 질문, 답변 불러오기
    @Transactional(readOnly = true)
    public AssistantResponseDto getQuestionsAndAnswers(User user) {
        List<AssistantQuestion> questions = assistantQuestionRepository.findAllWithAnswerByUserId(user.getId());

        return AssistantResponseDto.fromEntity(questions);
    }
}
