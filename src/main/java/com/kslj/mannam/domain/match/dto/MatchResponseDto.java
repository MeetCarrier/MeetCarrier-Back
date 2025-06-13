package com.kslj.mannam.domain.match.dto;

import com.kslj.mannam.domain.chat.dto.LastChatDto;
import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 매칭 조회 시 반환할 데이터
public class MatchResponseDto {
    long id;
    LocalDateTime matchedAt;
    MatchStatus status;
    boolean agreed;
    long sessionId;
    Long roomId;
    Long user1Id;
    String user1Nickname;
    String user1ImageUrl;
    long user2Id;
    String user2Nickname;
    String user2ImageUrl;
    String lastMessage;
    LocalDateTime lastMessageAt;
    int unreadCount;


    public static MatchResponseDto fromEntity(Match match, Long sessionId, Long roomId, User user, LastChatDto lastChatInfo) {
        boolean agreed = match.isEntered(user);

        MatchResponseDto.MatchResponseDtoBuilder builder = MatchResponseDto.builder()
                .id(match.getId())
                .matchedAt(match.getMatchedAt())
                .status(match.getStatus())
                .agreed(agreed)
                .sessionId(sessionId)
                .roomId(roomId)
                .user1Id(match.getUser1().getId())
                .user1Nickname(match.getUser1().getNickname())
                .user1ImageUrl(match.getUser1().getImgUrl())
                .user2Id(match.getUser2().getId())
                .user2Nickname(match.getUser2().getNickname())
                .user2ImageUrl(match.getUser2().getImgUrl());

        if (lastChatInfo != null) {
            builder.unreadCount(lastChatInfo.getUnreadCount());
            builder.lastMessage(lastChatInfo.getLastMessage());
            builder.lastMessageAt(lastChatInfo.getLastMessageAt());
        }

        return builder.build();
    }
}
