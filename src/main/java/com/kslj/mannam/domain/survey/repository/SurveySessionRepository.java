package com.kslj.mannam.domain.survey.repository;

import com.kslj.mannam.domain.survey.entity.SurveySession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SurveySessionRepository extends JpaRepository<SurveySession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SurveySession s where s.id = :id")
    Optional<SurveySession> findByIdForUpdate(@Param("id") Long id);
}
