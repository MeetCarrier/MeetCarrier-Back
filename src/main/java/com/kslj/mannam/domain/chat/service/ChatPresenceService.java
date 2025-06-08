package com.kslj.mannam.domain.chat.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatPresenceService {

    private final Map<Long, Set<Long>> activeUserMap = new ConcurrentHashMap<>();

    public void userJoined(Long roomId, Long userId) {
        activeUserMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void userLeft(Long roomId, Long userId) {
        if (activeUserMap.containsKey(roomId)) {
            activeUserMap.get(roomId).remove(userId);
            if (activeUserMap.get(roomId).isEmpty()) {
                activeUserMap.remove(roomId);
            }
        }
    }

    public boolean isUserActive(long roomId, Long userId) {
        if (activeUserMap.get(roomId) == null) {
            return false;
        }

        return activeUserMap.get(roomId).contains(userId);
    }
}
