package com.kslj.mannam.domain.chat.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatPresenceService {

    // 각 채팅방에 접속 중인 유저 저장
    private final Map<Long, Set<Long>> activeUserMap = new ConcurrentHashMap<>();

    // 채팅방에 유저 입장
    public void userJoined(Long roomId, Long userId) {
        activeUserMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    // 채팅방에서 유저 퇴장
    public void userLeft(Long roomId, Long userId) {
        if (activeUserMap.containsKey(roomId)) {
            activeUserMap.get(roomId).remove(userId);
            if (activeUserMap.get(roomId).isEmpty()) {
                activeUserMap.remove(roomId);
            }
        }
    }

    // 채팅방에 유저 입장해있는지 확인
    public boolean isUserActive(long roomId, Long userId) {
        if (activeUserMap.get(roomId) == null) {
            return false;
        }

        return activeUserMap.get(roomId).contains(userId);
    }
}
