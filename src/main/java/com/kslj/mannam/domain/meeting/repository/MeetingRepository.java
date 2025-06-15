package com.kslj.mannam.domain.meeting.repository;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.meeting.entity.Meeting;
import com.kslj.mannam.domain.meeting.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("SELECT m FROM Meeting m " +
            "JOIN FETCH m.match ma " +
            "JOIN FETCH ma.user1 " +
            "JOIN FETCH ma.user2 " +
            "WHERE ma.user1.id = :userId OR ma.user2.id = :userId")
    List<Meeting> findAllByUserId(@Param("userId") Long userId);

    List<Meeting> findByDateBetween(LocalDateTime dateAfter, LocalDateTime dateBefore);

    boolean existsByMatchIdAndStatus(Long matchId, MeetingStatus status);

    Meeting findByMatch(Match match);
}
