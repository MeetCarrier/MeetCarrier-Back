package com.kslj.mannam.domain.room.repository;

import com.kslj.mannam.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room getRoomByMatchId(Long matchId);
}
