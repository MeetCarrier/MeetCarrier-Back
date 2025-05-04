package com.kslj.mannam.domain.room.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    // 채팅방 생성
    public long createRoom(Match match) {
        Room newRoom = Room.builder()
                .match(match)
                .build();

        Room savedRoom = roomRepository.save(newRoom);

        return savedRoom.getId();
    }

    // 채팅방 ID 조회
    public long getRoomId(long matchId) {
        Room room = roomRepository.getRoomByMatchId(matchId);

        return room.getId();
    }
}
