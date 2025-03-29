package com.kslj.mannam.domain.journal.repository;

import com.kslj.mannam.domain.journal.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {
}
