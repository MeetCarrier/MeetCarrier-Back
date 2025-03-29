package com.kslj.mannam.domain.survey.repository;

import com.kslj.mannam.domain.survey.entity.SurveySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveySessionRepository extends JpaRepository<SurveySession, Long> {
}
