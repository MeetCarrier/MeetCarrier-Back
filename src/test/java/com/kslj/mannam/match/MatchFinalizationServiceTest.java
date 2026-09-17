package com.kslj.mannam.match;

import com.kslj.mannam.domain.match.dto.MatchFinalizationResult;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.match.service.MatchFinalizationService;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.notification.repository.NotificationOutboxRepository;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchFinalizationServiceTest {
    @Mock UserRepository userRepository;
    @Mock MatchRepository matchRepository;
    @Mock MatchService matchService;
    @Mock SurveyService surveyService;
    @Mock NotificationOutboxRepository outboxRepository;
    MatchFinalizationService service;

    @BeforeEach
    void setUp() {
        service = new MatchFinalizationService(userRepository, matchRepository, matchService, surveyService, outboxRepository);
    }

    @Test
    void locksUsersInIdOrderAndCreatesSingleMatch() {
        User first = User.builder().id(1L).build();
        User second = User.builder().id(2L).build();
        when(userRepository.findAllByIdForUpdate(List.of(1L, 2L))).thenReturn(List.of(first, second));
        when(matchRepository.existsActiveMatchForUsers(eq(List.of(1L, 2L)), anyList())).thenReturn(false);
        when(matchService.createMatch(any())).thenReturn(10L);
        when(surveyService.createSurveySession(10L)).thenReturn(20L);

        MatchFinalizationResult result = service.finalizeMatch(2L, 1L, 0.7);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSurveySessionId()).isEqualTo(20L);
        verify(userRepository).findAllByIdForUpdate(List.of(1L, 2L));
        verify(matchService, times(1)).createMatch(any());
        verify(outboxRepository, times(2)).save(any());
    }

    @Test
    void activeMatchRejectsSecondFinalization() {
        User first = User.builder().id(1L).build();
        User second = User.builder().id(2L).build();
        when(userRepository.findAllByIdForUpdate(List.of(1L, 2L))).thenReturn(List.of(first, second));
        when(matchRepository.existsActiveMatchForUsers(eq(List.of(1L, 2L)), anyList())).thenReturn(true);

        MatchFinalizationResult result = service.finalizeMatch(1L, 2L, 0.7);

        assertThat(result.isSuccess()).isFalse();
        verify(matchService, never()).createMatch(any());
    }
}
