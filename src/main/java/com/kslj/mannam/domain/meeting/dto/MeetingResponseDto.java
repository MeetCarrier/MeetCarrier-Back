package com.kslj.mannam.domain.meeting.dto;

import com.kslj.mannam.domain.meeting.entity.Meeting;
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
    LocalDateTime date;
    String location;
    String note;

    public static MeetingResponseDto fromEntity(Meeting meeting) {
        return MeetingResponseDto.builder()
                .id(meeting.getId())
                .date(meeting.getDate())
                .location(meeting.getLocation())
                .note(meeting.getNote())
                .build();
    }
}
