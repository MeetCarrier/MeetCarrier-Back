package com.kslj.mannam.domain.survey.repository;

import com.kslj.mannam.domain.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
}
