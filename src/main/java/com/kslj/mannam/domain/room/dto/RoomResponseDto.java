package com.kslj.mannam.domain.room.dto;

import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.RoomStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    private RoomStatus status;
    private LocalDateTime deactivationTime;

    public static RoomResponseDto fromEntity(Room room) {
        return RoomResponseDto.builder()
                .status(room.getStatus())
                .deactivationTime(room.getDeactivationTime())
                .build();
    }
}
