package com.kslj.mannam.domain.room.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.RoomStatus;
import com.kslj.mannam.domain.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;

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

    // 채팅방 종료시간을 지났으면 해당 채팅방 비활성화
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkRoomTime() {
        log.info("Room Time Check Started");
        List<Room> rooms = roomRepository.findAllByStatusAndDeactivationTimeBefore(RoomStatus.Activate, LocalDateTime.now());

        for (Room room : rooms) {
            room.getMatch().updateStatus(MatchStatus.Reviewing);
            room.updateStatus(RoomStatus.Deactivate);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class RoomNotFoundException extends EntityNotFoundException {
        public RoomNotFoundException(Long roomId) {
            super("채팅방을 찾을 수 없습니다. roomId=" + roomId);
        }
    }
}
