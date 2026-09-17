package com.kslj.mannam.domain.room.repository;

import com.kslj.mannam.domain.room.entity.Room;
import com.kslj.mannam.domain.room.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room getRoomByMatchId(Long matchId);
    List<Room> findAllByStatusAndDeactivationTimeBefore(RoomStatus status, LocalDateTime deactivationTimeBefore);

    Optional<Room> findRoomByMatchId(Long id);

    @Query("""
            select count(r) > 0
            from Room r
            where r.id = :roomId
              and (r.match.user1.id = :userId or r.match.user2.id = :userId)
            """)
    boolean existsParticipant(@Param("roomId") long roomId, @Param("userId") long userId);
}
