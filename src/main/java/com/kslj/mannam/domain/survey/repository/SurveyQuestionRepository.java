package com.kslj.mannam.domain.survey.repository;

import com.kslj.mannam.domain.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
}
