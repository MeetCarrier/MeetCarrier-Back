package com.kslj.mannam.domain.match.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kslj.mannam.domain.match.dto.MatchQueueRequestDto;
import com.kslj.mannam.domain.match.dto.MatchRequestStateDto;
import com.kslj.mannam.domain.match.dto.MatchingQueueEntry;
import com.kslj.mannam.domain.match.enums.MatchRequestState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchQueueStore {
    private static final String WAITING_KEY = "matching:waiting";
    private static final Duration STATE_TTL = Duration.ofHours(1);
    private static final Duration WAITING_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MatchQueueStore(StringRedisTemplate redisTemplate,
                           @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public MatchRequestStateDto register(long userId, UUID requestId, LocalDateTime requestedAt) {
        String key = stateKey(userId);
        String script = "local s=redis.call('HGET',KEYS[1],'status'); " +
                "if s=='QUEUED' or s=='PROCESSING' or s=='WAITING' or s=='RESERVED' then return 0 end; " +
                "redis.call('HSET',KEYS[1],'requestId',ARGV[1],'userId',ARGV[2],'status','QUEUED','requestedAt',ARGV[3]); " +
                "redis.call('EXPIRE',KEYS[1],ARGV[4]); return 1";
        Long created = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), List.of(key),
                requestId.toString(), Long.toString(userId), requestedAt.toString(), Long.toString(STATE_TTL.toSeconds()));
        MatchRequestStateDto state = getState(userId).orElseThrow();
        state.setDuplicate(created == null || created == 0);
        state.setMessage(state.isDuplicate() ? "이미 처리 중인 매칭 요청입니다." : "매칭 요청이 접수되었습니다.");
        return state;
    }

    public Optional<MatchRequestStateDto> getState(long userId) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(stateKey(userId));
        if (values.isEmpty()) return Optional.empty();
        return Optional.of(MatchRequestStateDto.builder()
                .requestId(UUID.fromString((String) values.get("requestId")))
                .userId(Long.parseLong((String) values.get("userId")))
                .status(MatchRequestState.valueOf((String) values.get("status")))
                .requestedAt(LocalDateTime.parse((String) values.get("requestedAt")))
                .build());
    }

    public boolean updateState(long userId, UUID requestId, MatchRequestState status) {
        String script = "if redis.call('HGET',KEYS[1],'requestId')~=ARGV[1] then return 0 end; " +
                "redis.call('HSET',KEYS[1],'status',ARGV[2]); redis.call('EXPIRE',KEYS[1],ARGV[3]); return 1";
        Long changed = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), List.of(stateKey(userId)),
                requestId.toString(), status.name(), Long.toString(STATE_TTL.toSeconds()));
        return changed != null && changed == 1;
    }

    public boolean transitionState(long userId, UUID requestId, MatchRequestState expected, MatchRequestState next) {
        String script = "if redis.call('HGET',KEYS[1],'requestId')~=ARGV[1] then return 0 end; " +
                "if redis.call('HGET',KEYS[1],'status')~=ARGV[2] then return 0 end; " +
                "redis.call('HSET',KEYS[1],'status',ARGV[3]); redis.call('EXPIRE',KEYS[1],ARGV[4]); return 1";
        Long changed = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), List.of(stateKey(userId)),
                requestId.toString(), expected.name(), next.name(), Long.toString(STATE_TTL.toSeconds()));
        return changed != null && changed == 1;
    }

    public boolean startProcessing(long userId, UUID requestId) {
        MatchRequestStateDto state = getState(userId).orElse(null);
        if (state == null || !state.getRequestId().equals(requestId)) return false;
        if (state.getStatus() == MatchRequestState.PROCESSING) return true;
        return transitionState(userId, requestId, MatchRequestState.QUEUED, MatchRequestState.PROCESSING);
    }

    public void addWaiting(MatchingQueueEntry entry, UUID requestId) {
        long userId = entry.getUserData().getUserId();
        if (!transitionState(userId, requestId, MatchRequestState.PROCESSING, MatchRequestState.WAITING)) return;
        redisTemplate.opsForValue().set(waitingDataKey(userId), write(entry), WAITING_TTL);
        redisTemplate.opsForZSet().add(WAITING_KEY, Long.toString(userId), toEpochMillis(entry.getJoinTime()));
    }

    public List<MatchingQueueEntry> getWaitingEntries() {
        Set<String> ids = redisTemplate.opsForZSet().range(WAITING_KEY, 0, -1);
        if (ids == null) return List.of();
        List<MatchingQueueEntry> result = new ArrayList<>();
        for (String id : ids) {
            String json = redisTemplate.opsForValue().get(waitingDataKey(Long.parseLong(id)));
            if (json != null) result.add(read(json, MatchingQueueEntry.class));
        }
        return result;
    }

    public boolean reserve(long requesterId, UUID requestId, long candidateId) {
        String script = "if redis.call('HGET',KEYS[1],'requestId')~=ARGV[1] then return 0 end; " +
                "if redis.call('HGET',KEYS[1],'status')~='PROCESSING' then return 0 end; " +
                "if redis.call('HGET',KEYS[2],'status')~='WAITING' then return 0 end; " +
                "if not redis.call('ZSCORE',KEYS[3],ARGV[2]) then return 0 end; " +
                "redis.call('HSET',KEYS[1],'status','RESERVED','partnerId',ARGV[2]); " +
                "redis.call('HSET',KEYS[2],'status','RESERVED','partnerId',ARGV[3]); " +
                "redis.call('ZREM',KEYS[3],ARGV[2]); return 1";
        Long reserved = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(stateKey(requesterId), stateKey(candidateId), WAITING_KEY), requestId.toString(),
                Long.toString(candidateId), Long.toString(requesterId));
        return reserved != null && reserved == 1;
    }

    public boolean recoverReservation(long requesterId, UUID requestId) {
        Object partnerValue = redisTemplate.opsForHash().get(stateKey(requesterId), "partnerId");
        if (partnerValue == null) return false;
        long partnerId = Long.parseLong((String) partnerValue);
        restoreWaiting(partnerId);
        boolean restored = restoreProcessing(requesterId, requestId);
        if (restored) {
            redisTemplate.opsForHash().delete(stateKey(requesterId), "partnerId");
            redisTemplate.opsForHash().delete(stateKey(partnerId), "partnerId");
        }
        return restored;
    }

    public boolean cancel(long userId) {
        Optional<MatchRequestStateDto> current = getState(userId);
        if (current.isEmpty() || !current.get().getStatus().isActive()) return false;
        if (!transitionState(userId, current.get().getRequestId(), current.get().getStatus(), MatchRequestState.CANCELLED)) {
            return false;
        }
        removeWaitingData(userId);
        return true;
    }

    public void markMatched(long userId) {
        redisTemplate.opsForHash().put(stateKey(userId), "status", MatchRequestState.MATCHED.name());
        redisTemplate.opsForHash().delete(stateKey(userId), "partnerId");
        redisTemplate.expire(stateKey(userId), STATE_TTL);
        removeWaitingData(userId);
    }

    public void restoreWaiting(long userId) {
        MatchRequestStateDto state = getState(userId).orElse(null);
        if (state == null || !transitionState(userId, state.getRequestId(), MatchRequestState.RESERVED, MatchRequestState.WAITING)) return;
        String json = redisTemplate.opsForValue().get(waitingDataKey(userId));
        if (json != null) {
            MatchingQueueEntry entry = read(json, MatchingQueueEntry.class);
            redisTemplate.opsForZSet().add(WAITING_KEY, Long.toString(userId), toEpochMillis(entry.getJoinTime()));
        }
        redisTemplate.opsForHash().delete(stateKey(userId), "partnerId");
    }

    public boolean restoreProcessing(long userId, UUID requestId) {
        boolean restored = transitionState(userId, requestId, MatchRequestState.RESERVED, MatchRequestState.PROCESSING);
        if (restored) redisTemplate.opsForHash().delete(stateKey(userId), "partnerId");
        return restored;
    }

    public void updateScoresBidirectionally(long requesterId, List<com.kslj.mannam.domain.match.dto.FilterResultDto> results) {
        for (var result : results) {
            redisTemplate.opsForHash().put(scoreKey(requesterId), Long.toString(result.getUserId()), Double.toString(result.getFinalScore()));
            redisTemplate.opsForHash().put(scoreKey(result.getUserId()), Long.toString(requesterId), Double.toString(result.getFinalScore()));
            redisTemplate.expire(scoreKey(requesterId), WAITING_TTL);
            redisTemplate.expire(scoreKey(result.getUserId()), WAITING_TTL);
        }
    }

    public List<Long> getTopMatches(long userId) {
        Map<Object, Object> scores = redisTemplate.opsForHash().entries(scoreKey(userId));
        return scores.entrySet().stream()
                .sorted(Comparator.comparingDouble((Map.Entry<Object, Object> e) -> Double.parseDouble((String) e.getValue())).reversed())
                .limit(2).map(e -> Long.parseLong((String) e.getKey())).collect(Collectors.toList());
    }

    public Map<Long, Double> getScores(long userId) {
        Map<Long, Double> result = new HashMap<>();
        redisTemplate.opsForHash().entries(scoreKey(userId)).forEach((key, value) ->
                result.put(Long.parseLong((String) key), Double.parseDouble((String) value)));
        return result;
    }

    public List<Long> findTimedOutUsers(LocalDateTime threshold) {
        Set<String> ids = redisTemplate.opsForZSet().rangeByScore(WAITING_KEY, 0, toEpochMillis(threshold));
        if (ids == null) return List.of();
        return ids.stream().map(Long::parseLong).toList();
    }

    public void timeout(long userId) {
        MatchRequestStateDto state = getState(userId).orElse(null);
        if (state != null && transitionState(userId, state.getRequestId(),
                MatchRequestState.WAITING, MatchRequestState.TIMED_OUT)) {
            removeWaitingData(userId);
        }
    }

    public boolean isActive(long userId) {
        return getState(userId).map(s -> s.getStatus().isActive()).orElse(false);
    }

    public void addWaitingDirectly(MatchQueueRequestDto userData) {
        UUID requestId = UUID.randomUUID();
        register(userData.getUserId(), requestId, LocalDateTime.now());
        startProcessing(userData.getUserId(), requestId);
        addWaiting(MatchingQueueEntry.builder().userData(userData).joinTime(LocalDateTime.now()).build(), requestId);
    }

    private void removeWaitingData(long userId) {
        redisTemplate.opsForZSet().remove(WAITING_KEY, Long.toString(userId));
        redisTemplate.delete(waitingDataKey(userId));
        redisTemplate.delete(scoreKey(userId));
    }

    private long toEpochMillis(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private String write(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { throw new IllegalStateException("매칭 대기 정보를 직렬화할 수 없습니다.", e); }
    }

    private <T> T read(String value, Class<T> type) {
        try { return objectMapper.readValue(value, type); }
        catch (JsonProcessingException e) { throw new IllegalStateException("매칭 대기 정보를 역직렬화할 수 없습니다.", e); }
    }

    private String stateKey(long userId) { return "matching:request:" + userId; }
    private String waitingDataKey(long userId) { return "matching:waiting:data:" + userId; }
    private String scoreKey(long userId) { return "matching:scores:" + userId; }
}
