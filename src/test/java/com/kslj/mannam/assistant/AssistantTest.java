package com.kslj.mannam.assistant;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import com.kslj.mannam.domain.assistant.repository.AssistantAnswerRepository;
import com.kslj.mannam.domain.assistant.repository.AssistantQuestionRepository;
import com.kslj.mannam.domain.assistant.service.AssistantService;
import com.kslj.mannam.domain.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class AssistantTest {

    @Autowired
    private AssistantService assistantService;

    @MockitoBean
    private AssistantQuestionRepository assistantQuestionRepository;

    @MockitoBean
    private AssistantAnswerRepository assistantAnswerRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TestUtils testUtils;

    // 질문 전송 테스트 (Mock 테스트)
    @Test
    public void createQuestionAndSendToAITest() {
        // given
        User foundUser = testUtils.createAndGetTestUser();
        String question = "테스트 질문입니다.";

        AssistantQuestion savedQuestion = AssistantQuestion.builder()
                .id(100L)
                .content(question)
                .user(foundUser)
                .build();

        when(assistantQuestionRepository.save(any(AssistantQuestion.class))).thenReturn(savedQuestion);

        // when
        long questionId = assistantService.createQuestionAndSendToAI(foundUser, question);


        // then
        Assertions.assertEquals(questionId, savedQuestion.getId());

        verify(assistantQuestionRepository, times(1)).save(any(AssistantQuestion.class));

        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate, times(1)).convertAndSend("ai_request_queue", eq(messageCaptor.capture()));

        Map<String, Object> message = messageCaptor.getValue();
        Assertions.assertEquals(questionId, message.get("questionId"));
        Assertions.assertEquals(question, message.get("question"));

    }

    // 응답 수신 테스트 (Mock 테스트)
    @Test
    public void createAnswerTest() {
        // given
        long questionId = 100L;
        String answerContent = "응답입니다.";

        AssistantQuestion question = AssistantQuestion.builder()
                .id(questionId)
                .content("테스트 질문입니다.")
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("questionId", questionId);
        response.put("answer", answerContent);

        when(assistantQuestionRepository.findById(questionId)).thenReturn(Optional.of(question));

        when(assistantAnswerRepository.save(any(AssistantAnswer.class))).thenAnswer(invocation -> {
            AssistantAnswer answer = invocation.getArgument(0);
            return answer.builder()
                    .id(200L)
                    .content(answer.getContent())
                    .question(answer.getQuestion())
                    .build();
        });

        // when
        assistantService.createAnswer(response);

        // then
        verify(assistantQuestionRepository, times(1)).findById(questionId);
        verify(assistantAnswerRepository, times(1)).save(any(AssistantAnswer.class));
    }
}
