package com.kslj.mannam.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kslj.mannam.domain.chat.enums.MessageType;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chats")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    private String message;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_read")
    @Builder.Default
    @JsonProperty("isRead")
    private Boolean isRead = false;

    @Column(name = "is_visible")
    @Builder.Default
    @JsonProperty("isVisible")
    private Boolean isVisible = true;

    @Builder.Default
    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateIsRead(boolean read) {
        this.isRead = read;
    }
}
