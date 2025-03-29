package com.kslj.mannam.domain.journal.repository;

import com.kslj.mannam.domain.journal.entity.JournalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalImageRepository extends JpaRepository<JournalImage, Long> {
}
