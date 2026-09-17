package com.kslj.mannam.domain.chat.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ChatPresenceService {

    private final Map<Long, Map<Long, Set<SubscriptionKey>>> subscriptionsByRoom = new HashMap<>();
    private final Map<String, Set<SubscriptionRef>> subscriptionsBySession = new HashMap<>();

    public synchronized void addSubscription(
            Long roomId,
            Long userId,
            String sessionId,
            String subscriptionId
    ) {
        if (roomId == null || userId == null || sessionId == null || subscriptionId == null) {
            return;
        }

        removeSubscriptionInternal(sessionId, subscriptionId);

        SubscriptionKey subscriptionKey = new SubscriptionKey(sessionId, subscriptionId);
        subscriptionsByRoom
                .computeIfAbsent(roomId, ignored -> new HashMap<>())
                .computeIfAbsent(userId, ignored -> new HashSet<>())
                .add(subscriptionKey);

        subscriptionsBySession
                .computeIfAbsent(sessionId, ignored -> new HashSet<>())
                .add(new SubscriptionRef(roomId, userId, subscriptionId));
    }

    public synchronized void removeSubscription(String sessionId, String subscriptionId) {
        if (sessionId == null || subscriptionId == null) {
            return;
        }

        removeSubscriptionInternal(sessionId, subscriptionId);
    }

    public synchronized void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Set<SubscriptionRef> subscriptions = subscriptionsBySession.remove(sessionId);
        if (subscriptions == null) {
            return;
        }

        for (SubscriptionRef subscription : subscriptions) {
            removeFromRoomIndex(
                    subscription.roomId(),
                    subscription.userId(),
                    new SubscriptionKey(sessionId, subscription.subscriptionId())
            );
        }
    }

    public synchronized boolean isUserActive(long roomId, Long userId) {
        Map<Long, Set<SubscriptionKey>> users = subscriptionsByRoom.get(roomId);
        if (users == null) {
            return false;
        }

        Set<SubscriptionKey> subscriptions = users.get(userId);
        return subscriptions != null && !subscriptions.isEmpty();
    }

    private void removeSubscriptionInternal(String sessionId, String subscriptionId) {
        Set<SubscriptionRef> sessionSubscriptions = subscriptionsBySession.get(sessionId);
        if (sessionSubscriptions == null) {
            return;
        }

        Set<SubscriptionRef> subscriptionsToRemove = new HashSet<>();
        for (SubscriptionRef subscription : sessionSubscriptions) {
            if (subscription.subscriptionId().equals(subscriptionId)) {
                subscriptionsToRemove.add(subscription);
            }
        }

        for (SubscriptionRef subscription : subscriptionsToRemove) {
            sessionSubscriptions.remove(subscription);
            removeFromRoomIndex(
                    subscription.roomId(),
                    subscription.userId(),
                    new SubscriptionKey(sessionId, subscription.subscriptionId())
            );
        }

        if (sessionSubscriptions.isEmpty()) {
            subscriptionsBySession.remove(sessionId);
        }
    }

    private void removeFromRoomIndex(Long roomId, Long userId, SubscriptionKey subscriptionKey) {
        Map<Long, Set<SubscriptionKey>> users = subscriptionsByRoom.get(roomId);
        if (users == null) {
            return;
        }

        Set<SubscriptionKey> subscriptions = users.get(userId);
        if (subscriptions == null) {
            return;
        }

        subscriptions.remove(subscriptionKey);
        if (subscriptions.isEmpty()) {
            users.remove(userId);
        }
        if (users.isEmpty()) {
            subscriptionsByRoom.remove(roomId);
        }
    }

    private record SubscriptionKey(String sessionId, String subscriptionId) {
    }

    private record SubscriptionRef(Long roomId, Long userId, String subscriptionId) {
    }
}
