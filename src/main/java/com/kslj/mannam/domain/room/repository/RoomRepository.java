package com.kslj.mannam.domain.room.repository;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room getRoomByMatchId(Long matchId);
    List<Room> findAllByStatusAndDeactivationTimeBefore(ChatStatus status, LocalDateTime deactivationTimeBefore);
}
