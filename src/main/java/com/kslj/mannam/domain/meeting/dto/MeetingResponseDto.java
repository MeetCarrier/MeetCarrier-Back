package com.kslj.mannam.domain.meeting.dto;

import com.kslj.mannam.domain.meeting.entity.Meeting;
import com.kslj.mannam.domain.meeting.enums.MeetingStatus;
import com.kslj.mannam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponseDto {
    Long id;
    String nickname;
    LocalDateTime date;
    String location;
    String note;
    MeetingStatus status;
    int updateCount;

    public static MeetingResponseDto fromEntity(Meeting meeting, User user) {
        return MeetingResponseDto.builder()
                .id(meeting.getId())
                .nickname(meeting.getMatch().getOtherUser(user).getNickname())
                .date(meeting.getDate())
                .location(meeting.getLocation())
                .note(meeting.getNote())
                .status(meeting.getStatus())
                .updateCount(3 - meeting.getUpdateCount())
                .build();
    }
}
