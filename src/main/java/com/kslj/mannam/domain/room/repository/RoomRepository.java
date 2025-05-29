package com.kslj.mannam.domain.room.repository;

import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room getRoomByMatchId(Long matchId);

    List<Room> getRoomByStatus(ChatStatus status);

    Room getRoomById(Long id);
}
