package com.kslj.mannam.survey;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.room.service.RoomService;
import com.kslj.mannam.domain.survey.entity.SurveySession;
import com.kslj.mannam.domain.survey.repository.SurveyAnswerRepository;
import com.kslj.mannam.domain.survey.repository.SurveyQuestionRepository;
import com.kslj.mannam.domain.survey.repository.SurveySessionRepository;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SurveyServiceAuthorizationTest {

    private SurveySessionRepository sessionRepository;
    private SurveyAnswerRepository answerRepository;
    private SimpMessagingTemplate messagingTemplate;
    private SurveyService surveyService;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SurveySessionRepository.class);
        answerRepository = mock(SurveyAnswerRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        surveyService = new SurveyService(
                sessionRepository,
                answerRepository,
                mock(SurveyQuestionRepository.class),
                mock(RoomService.class),
                mock(MatchService.class),
                messagingTemplate
        );
    }

    @Test
    void 세션참여자가아니면설문답변을제출할수없다() {
        SurveySession session = session(10L, user(1L), user(2L));
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> surveyService.submitSurveyAnswer(10L, List.of(), user(3L)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(answerRepository, messagingTemplate);
    }

    @Test
    void 세션참여자가아니면설문을중단할수없다() {
        SurveySession session = session(10L, user(1L), user(2L));
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> surveyService.leaveSession(10L, user(3L), "OTHER", null))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(answerRepository, messagingTemplate);
    }

    private SurveySession session(long id, User user1, User user2) {
        return SurveySession.builder()
                .id(id)
                .match(Match.builder().user1(user1).user2(user2).build())
                .build();
    }

    private User user(long id) {
        return User.builder().id(id).build();
    }
}
