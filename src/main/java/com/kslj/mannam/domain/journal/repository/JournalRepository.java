package com.kslj.mannam.domain.journal.repository;

import com.kslj.mannam.domain.journal.entity.Journal;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {
    List<Journal> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    boolean existsByUserAndCreatedAtBetween(User user, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}
