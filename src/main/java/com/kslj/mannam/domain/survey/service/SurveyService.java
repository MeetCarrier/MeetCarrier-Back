package com.kslj.mannam.domain.survey.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.dto.*;
import com.kslj.mannam.domain.survey.entity.SurveyAnswer;
import com.kslj.mannam.domain.survey.entity.SurveyQuestion;
import com.kslj.mannam.domain.survey.entity.SurveySession;
import com.kslj.mannam.domain.survey.enums.SessionStatus;
import com.kslj.mannam.domain.survey.repository.SurveyAnswerRepository;
import com.kslj.mannam.domain.survey.repository.SurveyQuestionRepository;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SurveyService {

    private final SurveySessionRepository surveySessionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final RoomService roomService;
    private final MatchService matchService;

    private final SimpMessagingTemplate messagingTemplate;

    // 설문지 세션 생성
    @Transactional
    public long createSurveySession(long matchId) {
        Match match = matchService.getMatch(matchId);
        matchService.updateMatchStatus(matchId, MatchStatus.Surveying);

        SurveySession surveySession = SurveySession.builder()
                .match(match)
                .build();

        SurveySession savedSession = surveySessionRepository.save(surveySession);

        return savedSession.getId();
    }

    // 설문지 세션 ID 조회
    @Transactional(readOnly = true)
    public long getSurveySessionId(long matchId) {
        SurveySession session = surveySessionRepository.findSurveySessionByMatchId(matchId);

        return session.getId();
    }

    // 설문지 질문 생성
    @Transactional
    public void createSurveyQuestions(long matchId, long sessionId) {
        SurveySession session = surveySessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("SurveySession not found: " + sessionId));

        Match match = matchService.getMatch(matchId);
        String user1Question = match.getUser1().getQuestion();
        String user2Question = match.getUser2().getQuestion();

        // 질문 랜덤하게 뽑기
        List<String> questionPool = loadQuestionsFromFile();
        Collections.shuffle(questionPool);
        List<String> selectedQuestions;

        // 설정한 질문이 둘 다 없는 경우 -> 5개, 설정한 질문이 둘 중 하나만 있는 경우 -> 4개, 둘 다 질문을 설정한 경우 -> 3개
        if(user1Question == null && user2Question == null) {
            selectedQuestions = questionPool.stream().limit(5).toList();
        }
        else if (user1Question != null && user2Question != null) {
            selectedQuestions = questionPool.stream().limit(3).toList();
        }
        else {
            selectedQuestions = questionPool.stream().limit(4).toList();
        }

        // 랜덤으로 뽑은 질문 저장
        for (String content : selectedQuestions) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .content(content)
                    .surveySession(session)
                    .build();
            surveyQuestionRepository.save(question);
        }

        // 사용자가 설정한 질문을 줄러와 저장
        if(user1Question != null) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .content(user1Question)
                    .surveySession(session)
                    .build();
            surveyQuestionRepository.save(question);
        }
        if(user2Question != null) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .content(user2Question)
                    .surveySession(session)
                    .build();
            surveyQuestionRepository.save(question);
        }
    }

    // 설문지 질문 조회
    @Transactional(readOnly = true)
    public List<SurveyQuestionResponseDto> getSurveyQuestions(long sessionId) {
        List<SurveyQuestionResponseDto> responseDtos = new ArrayList<>();

        List<SurveyQuestion> questions = surveyQuestionRepository.getSurveyQuestionBySurveySession_Id(sessionId);
        for(SurveyQuestion question : questions) {
            SurveyQuestionResponseDto responseDto = SurveyQuestionResponseDto.builder()
                    .questionId(question.getId())
                    .content(question.getContent())
                    .build();

            responseDtos.add(responseDto);
        }

        return responseDtos;
    }

    // 설문지 답변 등록
    @Transactional
    public void submitSurveyAnswer(long sessionId, List<SurveyAnswerRequestDto> answers, User user) {
        SurveySession session = surveySessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("SurveySession not found: " + sessionId));

        for(SurveyAnswerRequestDto answer : answers) {
            SurveyQuestion question = surveyQuestionRepository.getSurveyQuestionById(answer.getQuestionId());

            // 답변 등록
            SurveyAnswer surveyAnswer = SurveyAnswer.builder()
                    .content(answer.getContent())
                    .user(user)
                    .surveySession(session)
                    .surveyQuestion(question)
                    .build();

            surveyAnswerRepository.save(surveyAnswer);
        }

        session.incrementAnsweredCount();

        // 답변 수 체크
        if (session.getAnsweredCount() >= 2) {
            Match match = session.getMatch();

            long roomId = roomService.createRoom(match);
            matchService.updateMatchStatus(match.getId(), MatchStatus.Chatting); // 전체 상태 변경
            session.updateSessionStatus(SessionStatus.Done);

            notifyComplete(session.getId(), roomId);
        }
    }

    // 설문지 답변 조회
    @Transactional(readOnly = true)
    public List<SurveyAnswerResponseDto> getSurveyAnswers(long sessionId) {
        List<SurveyAnswer> surveyAnswers = surveyAnswerRepository.getSurveyAnswersBySurveySession_Id(sessionId);
        List<SurveyAnswerResponseDto> responseDtos = new ArrayList<>();

        for (SurveyAnswer surveyAnswer : surveyAnswers) {
            SurveyAnswerResponseDto responseDto = SurveyAnswerResponseDto.builder()
                    .content(surveyAnswer.getContent())
                    .questionId(surveyAnswer.getSurveyQuestion().getId())
                    .userId(surveyAnswer.getUser().getId())
                    .build();

            responseDtos.add(responseDto);
        }

        return responseDtos;
    }

    // 설문지 중 나가기
    @Transactional
    public void leaveSession(long sessionId, User user, String reasonCodes, String customReason) {
        SurveySession session = surveySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("SurveySession not found: " + sessionId));

        Match match = session.getMatch();

        match.cancelMatch(user, MatchStatus.Survey_Cancelled, reasonCodes, customReason);

        // 상대방에게 보낼 메시지 구성
        SurveyLeaveDto leaveDto = SurveyLeaveDto.builder()
                .sessionId(sessionId)
                .reasonCodes(reasonCodes)
                .customReason(customReason)
                .build();

        log.info("leaveDto = " + leaveDto);
        // 알림 전송
        messagingTemplate.convertAndSend("/topic/survey/" + sessionId + "/leave", leaveDto);
    }

    // 완료 알람 전송
    private void notifyComplete(long sessionId, long roomId) {
        // 전달할 데이터 생성
        SurveyCompleteDto dto = SurveyCompleteDto.builder()
                .sessionId(sessionId)
                .roomId(roomId)
                .build();

        // 클라이언트에게 메시지 전송
        System.out.println("dto = " + dto);
        messagingTemplate.convertAndSend("/topic/survey/" + sessionId + "/complete", dto);
    }

    // 질문 리스트 가져오기
    private List<String> loadQuestionsFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("questions.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("질문 리스트 가져오던 중 오류 발생", e);
        }
    }
}
