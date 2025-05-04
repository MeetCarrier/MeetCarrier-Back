package com.kslj.mannam.domain.room.repository;

import com.kslj.mannam.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room getRoomByMatchId(Long matchId);

    @Query("SELECT r FROM Room r JOIN FETCH r.match m JOIN FETCH m.user1 JOIN FETCH m.user2 WHERE r.id = :roomId")
    Optional<Room> findRoomWithMatchAndUsers(@Param("roomId") long roomId);
}
