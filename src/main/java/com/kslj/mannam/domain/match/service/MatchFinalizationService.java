package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchCreateDto;
import com.kslj.mannam.domain.match.dto.MatchFinalizationResult;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.notification.entity.NotificationOutbox;
import com.kslj.mannam.domain.notification.repository.NotificationOutboxRepository;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchFinalizationService {
    private static final List<MatchStatus> ACTIVE_STATUSES = List.of(
            MatchStatus.Matched, MatchStatus.Surveying, MatchStatus.Chatting, MatchStatus.Meeting);

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final SurveyService surveyService;
    private final NotificationOutboxRepository outboxRepository;

    @Transactional
    public MatchFinalizationResult finalizeMatch(long user1Id, long user2Id, double score) {
        if (user1Id == user2Id) return MatchFinalizationResult.conflict();

        List<Long> ids = new ArrayList<>(List.of(user1Id, user2Id));
        ids.sort(Comparator.naturalOrder());
        List<User> lockedUsers = userRepository.findAllByIdForUpdate(ids);
        if (lockedUsers.size() != 2 || matchRepository.existsActiveMatchForUsers(ids, ACTIVE_STATUSES)) {
            return MatchFinalizationResult.conflict();
        }

        long matchId = matchService.createMatch(MatchCreateDto.builder()
                .user1Id(user1Id).user2Id(user2Id).score(score).build());
        long sessionId = surveyService.createSurveySession(matchId);
        surveyService.createSurveyQuestions(matchId, sessionId);

        for (User user : lockedUsers) {
            outboxRepository.save(NotificationOutbox.builder()
                    .eventKey("MATCH:" + matchId + ":" + user.getId())
                    .user(user)
                    .referenceId(matchId)
                    .build());
        }
        return new MatchFinalizationResult(true, matchId, sessionId);
    }
}
