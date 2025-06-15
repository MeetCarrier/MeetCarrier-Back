package com.kslj.mannam.domain.room.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    // 채팅방 생성
    @Transactional
    public long createRoom(Match match) {
        Room newRoom = Room.builder()
                .match(match)
                .build();

        Room savedRoom = roomRepository.save(newRoom);

        return savedRoom.getId();
    }

    // 채팅방 ID 조회
    @Transactional(readOnly = true)
    public long getRoomId(long matchId) {
        Room room = roomRepository.getRoomByMatchId(matchId);

        return room.getId();
    }

    @Transactional(readOnly = true)
    public Room getRoom(long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class RoomNotFoundException extends EntityNotFoundException {
        public RoomNotFoundException(Long roomId) {
            super("채팅방을 찾을 수 없습니다. roomId=" + roomId);
        }
    }
}
