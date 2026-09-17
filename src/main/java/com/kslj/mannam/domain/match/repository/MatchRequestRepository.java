package com.kslj.mannam.domain.match.repository;

import com.kslj.mannam.domain.match.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MatchRequest r where r.id = :id")
    Optional<MatchRequest> findByIdForUpdate(@Param("id") long id);
}
