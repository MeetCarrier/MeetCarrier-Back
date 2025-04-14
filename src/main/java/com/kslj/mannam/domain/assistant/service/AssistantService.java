package com.kslj.mannam.domain.assistant.service;

import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import com.kslj.mannam.domain.assistant.repository.AssistantAnswerRepository;
import com.kslj.mannam.domain.assistant.repository.AssistantQuestionRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssistantService {

    private final AssistantAnswerRepository assistantAnswerRepository;
    private final AssistantQuestionRepository assistantQuestionRepository;
    private final RabbitTemplate rabbitTemplate;

    // 질문 등록 및 AI 비서로 전달
    public long createQuestionAndSendToAI(User user, String questionContent) {
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

        return savedQuestion.getId();
    }

    // AI 비서 답변 수신 및 등록
    @RabbitListener(queues = "ai_response_queue")
    public void createAnswer(Map<String, Object> response) {
        try {
            long questionId = ((Number) response.get("questionId")).longValue();
            String answer = (String) response.get("answer");

            AssistantQuestion question = assistantQuestionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다. questionId=" + questionId));

            AssistantAnswer newAnswer = AssistantAnswer.builder()
                    .content(answer)
                    .question(question)
                    .build();

            assistantAnswerRepository.save(newAnswer);

            // 여기까지 오면 정상 처리
        } catch (Exception e) {
            // 로깅만 하고, 예외를 밖으로 터뜨리지 않음
            // 에러가 있어도 메시지 큐에는 "ACK" 보내는 흐름
            log.error("Failed to process AI response", e);
        }
    }

    // 질문 삭제
    public long delete(long questionId) {
        assistantQuestionRepository.deleteById(questionId);

        return questionId;
    }

    // 질문, 답변 불러오기

}
