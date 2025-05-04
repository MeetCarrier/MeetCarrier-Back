package com.kslj.mannam.domain.survey.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.dto.SurveyAnswerRequestDto;
import com.kslj.mannam.domain.survey.dto.SurveyAnswerResponseDto;
import com.kslj.mannam.domain.survey.dto.SurveyCompleteDto;
import com.kslj.mannam.domain.survey.dto.SurveyQuestionResponseDto;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        SurveySession surveySession = SurveySession.builder()
                .match(matchService.getMatch(matchId))
                .build();

        SurveySession savedSession = surveySessionRepository.save(surveySession);

        return savedSession.getId();
    }

    // 설문지 질문 등록 (질문은 서버에서 처리? 클라이언트 측에서 랜덤하게 몇 개 뽑아서 나에게 전달?)
    @Transactional
    public void createSurveyQuestions(long sessionId) {
        SurveySession session = surveySessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("SurveySession not found: " + sessionId));

        // 임의 설정. 질문 생성 뒤 그 질문들의 ID 반환
        SurveyQuestion question1 = SurveyQuestion.builder()
                .content("질문1")
                .surveySession(session)
                .build();
        surveyQuestionRepository.save(question1);
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
    public void submitSurveyAnswer(long sessionId, SurveyAnswerRequestDto requestDto, User user) {
        SurveySession session = surveySessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("SurveySession not found: " + sessionId));
        SurveyQuestion question = surveyQuestionRepository.getSurveyQuestionById(requestDto.getQuestionId());

        // 답변 등록
        SurveyAnswer surveyAnswer = SurveyAnswer.builder()
                .content(requestDto.getContent())
                .user(user)
                .surveySession(session)
                .surveyQuestion(question)
                .build();

        surveyAnswerRepository.save(surveyAnswer);

        // 답변 수 체크
        session.incrementAnsweredCount();
        if (session.getAnsweredCount() >= 2) {
            Match match = session.getMatch();
            // 답변 수가 n개 이상이면 답변 완료 판단 -> 채팅방 생성 서비스 호출, 매칭 상태 갱신
            long roomId = roomService.createRoom(match);
            matchService.updateMatchStatus(match.getId(), MatchStatus.Chatting);
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
                    .build();

            responseDtos.add(responseDto);
        }

        return responseDtos;
    }

    public void notifyComplete(long sessionId, long roomId) {
        // 전달할 데이터 생성
        SurveyCompleteDto dto = SurveyCompleteDto.builder()
                .sessionId(sessionId)
                .roomId(roomId)
                .build();

        // 클라이언트에게 메시지 전송
        System.out.println("dto = " + dto);
        messagingTemplate.convertAndSend("/topic/survey/" + sessionId + "/complete", dto);
    }
}
