package com.kslj.mannam.domain.match.repository;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByUser1OrUser2(User user1, User user2);
}
