package com.kslj.mannam.domain.chat.repository;

import com.kslj.mannam.domain.chat.entity.Chat;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findChatByRoom(Room room);
    List<Chat> findChatByRoomAndType(Room room, MessageType type);

    @Modifying
    @Query("UPDATE Chat c SET c.isRead = true WHERE c.room.id = :roomId AND c.user.id != :currentUserId AND c.isRead = false")
    void markAsReadByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("currentUserId") Long currentUserId);

    @Query("SELECT c FROM Chat c WHERE c.room.id = :roomId ORDER BY c.sentAt DESC LIMIT 1")
    Optional<Chat> getLastChat(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(c) FROM Chat c WHERE c.room.id = :roomId AND c.user.id <> :userId AND c.isRead = false")
    Long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
