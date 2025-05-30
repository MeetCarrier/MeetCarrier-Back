package com.kslj.mannam.domain.match.repository;

import com.kslj.mannam.domain.match.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
}
