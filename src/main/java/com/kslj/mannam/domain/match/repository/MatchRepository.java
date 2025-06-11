package com.kslj.mannam.domain.match.repository;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByUser1OrUser2(User user1, User user2);

    @Query("SELECT m FROM Match m WHERE (m.user1 = :userA AND m.user2 = :userB) OR (m.user1 = :userB AND m.user2 = :userA)")
    Optional<Match> findMatchByUsers(@Param("userA") User userA, @Param("userB") User userB);

    Match getMatchById(Long id);
}
