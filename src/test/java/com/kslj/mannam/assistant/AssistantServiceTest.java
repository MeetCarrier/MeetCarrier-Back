package com.kslj.mannam.assistant;

import com.kslj.mannam.domain.assistant.dto.AssistantResponseDto;
import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import com.kslj.mannam.domain.assistant.service.AssistantService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class AssistantServiceTest {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private UserService userService;

    // 질문, 답변 조회 테스트
    @Test
    public void getQuestionAndAnswerTest() {
        User user = userService.getUserById(1);

        AssistantResponseDto questionsAndAnswers = assistantService.getQuestionsAndAnswers(user);

        for(AssistantQuestion question : questionsAndAnswers.getAssistantQuestions()) {
            System.out.println("question = " + question.getContent());
        }
        for(AssistantAnswer answer : questionsAndAnswers.getAssistantAnswers()) {
            System.out.println("answer = " + answer.getContent());
        }
        Assertions.assertEquals(5, questionsAndAnswers.getAssistantAnswers().size());
        Assertions.assertEquals(5, questionsAndAnswers.getAssistantQuestions().size());
    }
}
