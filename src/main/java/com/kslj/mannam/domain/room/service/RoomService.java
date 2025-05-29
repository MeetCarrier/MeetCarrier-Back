package com.kslj.mannam.domain.room.service;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.ChatStatus;
import com.kslj.mannam.domain.room.repository.RoomRepository;
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
    public boolean getRoomStatus(long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        return room.getStatus() == ChatStatus.Activate;
    }

    // 채팅방 생성된 시점으로부터 24시간 지났는지 검사. 지났으면 해당 채팅방 비활성화
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkRoomTime() {
        log.info("Room Time Check Started");
        LocalDateTime now = LocalDateTime.now();

        List<Room> rooms = roomRepository.getRoomByStatus(ChatStatus.Activate);

        for (Room room : rooms) {
            if (room.getCreatedAt().isAfter(now.plusHours(24))) {
                room.updateStatus(ChatStatus.Deactivate);
            }
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class RoomNotFoundException extends RuntimeException {
        public RoomNotFoundException(Long roomId) {
            super("채팅방을 찾을 수 없습니다. roomId=" + roomId);
        }
    }
}
