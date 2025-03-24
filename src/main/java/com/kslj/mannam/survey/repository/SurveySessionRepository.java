package com.kslj.mannam.survey.repository;

import com.kslj.mannam.survey.entity.SurveySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveySessionRepository extends JpaRepository<SurveySession, Long> {
}
