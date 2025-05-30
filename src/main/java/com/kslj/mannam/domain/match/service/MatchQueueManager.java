package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.block.dto.BlockResponseDto;
import com.kslj.mannam.domain.block.service.BlockService;
import com.kslj.mannam.domain.match.dto.*;
import com.kslj.mannam.domain.review.dto.ReviewByReviewerIdDto;
import com.kslj.mannam.domain.review.dto.ReviewQueueDto;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.domain.survey.service.SurveyService;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchQueueManager {

    private final MatchService matchService;
    private final TestService testService;
    private final ReviewService reviewService;
    private final SurveyService surveyService;
    private final BlockService blockService;

    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private final LinkedHashMap<Long, MatchingQueueEntry> matchingQueue = new LinkedHashMap<>();
    private final Map<UUID, MatchingQueueEntry> requestMap = new ConcurrentHashMap<>();
    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT_SECONDS = 300;

    // 매칭 큐에 새로운 유저 접근
    @Transactional
    public void addNewUser(User user) {

        // 유저 정보 바탕으로 MatchQueueRequestDto 생성
        TestResponseDto test = testService.getTestByUserId(user).get(0);
        List<ReviewQueueDto> reviews = new ArrayList<>();
        for(ReviewByReviewerIdDto reviewDto : reviewService.getReviewByReviewerId(user.getId())) {
            ReviewQueueDto review = ReviewQueueDto.builder()
                    .rating(reviewDto.getRating())
                    .userId(reviewDto.getUserId())
                    .step(reviewDto.getStep())
                    .build();

            reviews.add(review);
        }

        MatchQueueRequestDto newUserData = MatchQueueRequestDto.builder()
                .userId(user.getId())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .interests(user.getInterests())
                .gender(user.getGender())
                .age(user.getAge())
                .phone(user.getPhone())
                .depressionScore(test.getDepressionScore())
                .efficacyScore(test.getEfficacyScore())
                .relationshipScore(test.getRelationshipScore())
                .reviews(reviews)
                .build();

        // 기존 매칭 큐 + newEntry -> python 필터링 호출
        MatchingQueueEntry newEntry = MatchingQueueEntry.builder()
                .userData(newUserData)
                .joinTime(LocalDateTime.now())
                .build();

        // 요청 ID 생성
        UUID requestId = UUID.randomUUID();

        List<String> blockedPhones = blockService.getBlocks(user).stream()
                .map(BlockResponseDto::getBlockedPhone)
                .toList();

        // 사용자가 설정한 나이차까지만 매칭 대상에 포함되도록 설정
        // 사용자가 이성 매칭 OFF 시 동성만 매칭 대상에 포함되도록 설정
        // 블락한 유저는 매칭 대상에서 제외하도록 설정
        // 설정한 거리 제한 이내의 유저만 포함되도록 설정
        int maxAgeGap = user.getMaxAgeGap();
        boolean allowOppositeGender = user.isAllowOppositeGender();
        Gender userGender = user.getGender();
        double maxDistance = user.getMaxMatchingDistance();
        double newUserLat = user.getLatitude();
        double newUserLng = user.getLongitude();

        List<MatchQueueRequestDto> waitingUsers = matchingQueue.values().stream()
                .map(MatchingQueueEntry::getUserData)
                .filter(userData -> {
                    // 나이 제한 조건
                    boolean ageMatch = userData.getAge() >= newUserData.getAge() - maxAgeGap &&
                            userData.getAge() <= newUserData.getAge() + maxAgeGap;

                    // 성별 조건
                    boolean genderMatch = allowOppositeGender || userData.getGender() == userGender;

                    // 블락 제외 조건
                    boolean notBlocked = !blockedPhones.contains(userData.getPhone());

                    // 거리 조건
                    double distance = getDistance(newUserLat, newUserLng, userData.getLatitude(), userData.getLongitude());
                    boolean distanceMatch = distance <= maxDistance;

                    // 로그 출력
                    System.out.println("User: " + userData.getPhone() + " - Distance: " + distance + "km");

                    return ageMatch && genderMatch && notBlocked && distanceMatch;
                })
                .toList();

        MatchFilterRequestDto request = MatchFilterRequestDto.builder()
                .requestId(requestId)
                .waitingUsers(waitingUsers)
                .newUser(newUserData)
                .build();

        // 요청 정보 임시 저장
        requestMap.put(requestId, newEntry);

        // 파이썬으로 전달
        rabbitTemplate.convertAndSend("match_request_queue", request);
    }

    // 사용자 세션에 등록 (필터링 결과를 전달하기 위함)
    public void registerUserSession(long userId) {
        userSessionMap.put(userId, String.valueOf(userId));
    }

    // 받은 필터링 결과로 매칭 처리
    @Transactional
    @RabbitListener(queues = "match_response_queue")
    public void processNewUser(MatchFilterResponseDto responseDto) {
        UUID requestId = responseDto.getRequestId();
        MatchingQueueEntry newEntry = requestMap.remove(requestId);

        if (newEntry == null) {
            // 요청과 매칭되는 사용자가 없음
            return;
        }

        List<FilterResultDto> filterResults = responseDto.getFilterResults();
        MatchQueueRequestDto newQueueDto = newEntry.getUserData();
        long requesterId = newQueueDto.getUserId();

        // 받은 필터링 결과가 비어있음 -> 큐에 사람이 없어 비교 불가
        if(filterResults.isEmpty()) {
            // 바로 대기 큐에 추가
            matchingQueue.put(requesterId, newEntry);
            return;
        }

        // 가장 높은 점수 가져오기
        FilterResultDto bestMatch = filterResults.get(0);

        System.out.println("bestMatch = " + bestMatch.toString());

        // 점수 비교
        if (bestMatch.getFinalScore() >= 0.5) {
            // 매칭 성공
            completeMatching(bestMatch, requesterId);
        } else {
            // 매칭 실패 -> 대기 큐에 추가
            matchingQueue.put(requesterId, newEntry);

            // 기존 유저들의 scoreMap 업데이트
            updateScoresWithNewUser(requesterId, filterResults);
        }
    }

    // 매칭 성공 처리
    @Transactional
    protected void completeMatching(FilterResultDto matchedInfo, long userId) {
        // 매칭된 유저 큐에서 삭제
        removeUser(matchedInfo.getUserId());

        // 매칭 엔티티 생성
        long matchId = matchService.createMatch(MatchCreateDto.builder()
                .score(matchedInfo.getFinalScore())
                .user1Id(userId)
                .user2Id(matchedInfo.getUserId())
                .build()
        );

        long surveySessionId = surveyService.createSurveySession(matchId);
        surveyService.createSurveyQuestions(matchId, surveySessionId);

        // 매칭 성공 메시지 전송 (신규 유저)
        sendMatchSuccess(userId, matchedInfo.getUserId(), matchedInfo.getFinalScore(), surveySessionId);

        // 매칭 성공 메시지 전송 (기존 유저)
        sendMatchSuccess(matchedInfo.getUserId(), userId, matchedInfo.getFinalScore(), surveySessionId);
    }

    // 매칭 성공한 유저 삭제
    public void removeUser(long userId) {
        // 매칭 큐에서 삭제
        matchingQueue.remove(userId);

        // 다른 유저들의 scoreMap에서 삭제
        for (MatchingQueueEntry entry : matchingQueue.values()) {
            if (entry.getScoreMap() != null) {
                entry.getScoreMap().remove(userId);
            }
        }
    }

    // 매칭 성공 시 메시지 전달
    private void sendMatchSuccess(long receiverId, long matchedUserId, double score, long surveySessionId) {
        MatchResultDto result = MatchResultDto.builder()
                .matchedUserId(matchedUserId)
                .finalScore(score)
                .surveySessionId(surveySessionId)
                .build();

        log.info("{}에게 매칭 성공 메시지 전달(상대방 ID: {})", receiverId, matchedUserId);

        // 실제로 로그인하고 사용할 때 이용
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId), // userId 기준
                "/topic/match-result", // 클라이언트 구독 경로
                result
        );
    }

    // 매칭 점수 저장
    public void updateScoresWithNewUser(long userId, List<FilterResultDto> filterResults) {
        for (FilterResultDto result : filterResults) {
            long existingUserId = result.getUserId();
            double finalScore = result.getFinalScore();

            MatchingQueueEntry existingEntry = matchingQueue.get(existingUserId);
            if (existingEntry != null) {
                existingEntry.getScoreMap().put(userId, finalScore);
            }
        }
    }

    // 특정 유저의 상위 2명 매칭 대상 찾기
    public List<Long> getTopMatches(long userId) {
        MatchingQueueEntry entry = matchingQueue.get(userId);
        if (entry == null || entry.getScoreMap().isEmpty()) {
            return Collections.emptyList();
        }

        return entry.getScoreMap().entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())  // 점수 내림차순 정렬
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 타임아웃된 유저 찾기(5초 마다 검사)
    @Scheduled(fixedDelay = 5000)
    public void checkTimeouts() {
        List<Long> timeoutUserIds = new ArrayList<>();

        // 큐에 있는 유저들 접속 시간 계산
        for (MatchingQueueEntry entry : matchingQueue.values()) {
            Duration duration = Duration.between(entry.getJoinTime(), LocalDateTime.now());
            if (duration.getSeconds() > TIMEOUT_SECONDS) {
                timeoutUserIds.add(entry.getUserData().getUserId());
            }
        }

        for (Long userId : timeoutUserIds) {
            handleTimeout(userId);
        }
    }

    // 타임아웃 처리
    private void handleTimeout(long userId) {
        // 상위 2명 매칭 대상 추천
        List<Long> recommendedUserIds = getTopMatches(userId);

        // 큐에서 제거
        removeUser(userId);

        // 매칭 실패 WebSocket 메시지 전송
        MatchTimeoutDto timeoutDto = MatchTimeoutDto.builder()
                .recommendedUserIds(recommendedUserIds)
                .message("매칭 실패. 추천 상대를 안내합니다.")
                .build();

        // 로그인까지 사용할 때 이용
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/topic/match-result",
                timeoutDto
        );
    }

    // 매칭 도충 취소
    public boolean cancelMatching(long userId) {
        MatchingQueueEntry removedEntry = matchingQueue.remove(userId);
        if (removedEntry != null) {
            for (MatchingQueueEntry entry : matchingQueue.values()) {
                if (entry.getScoreMap() != null) {
                    entry.getScoreMap().remove(userId);
                }
            }

            log.info("❌ userId: {} 매칭 취소", userId);
            return true;
        }

        log.info("❗ userId: {}가 매칭 큐에 없습니다.", userId);
        return false;
    }

    // 테스트 용 대기 유저 추가
    public void addWaitingUserDirectly(MatchQueueRequestDto userData) {
        MatchingQueueEntry entry = MatchingQueueEntry.builder()
                .userData(userData)
                .joinTime(LocalDateTime.now())
                .build();
        matchingQueue.put(userData.getUserId(), entry);
    }

    // 큐 확인
    public List<WaitingUserInfoDto> getWaitingUsers() {
        return matchingQueue.values().stream()
                .map(entry -> WaitingUserInfoDto.builder()
                        .userData(entry.getUserData())
                        .scoreMap(new HashMap<>(entry.getScoreMap())) // 복사해서 넘겨주는 게 안전
                        .build())
                .collect(Collectors.toList());
    }

    // 매칭 큐에 유저 들어가있는지 검사
    public boolean isUserInQueue(long userId) {
        return matchingQueue.containsKey(userId);
    }

    // 위도, 경도로부터 거리 구하기
    public double getDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
