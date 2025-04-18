package com.kslj.mannam.domain.meeting.repository;

import com.kslj.mannam.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("""
           SELECT me FROM Meeting me JOIN me.match m WHERE m.user1.id = :userId OR m.user2.id = :userId
           """)
    List<Meeting> findAllByUserId(@Param("userId") Long userId);

    boolean existsByMatchId(Long matchId);
}
