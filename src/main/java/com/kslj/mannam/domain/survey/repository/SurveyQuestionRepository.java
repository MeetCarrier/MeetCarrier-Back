package com.kslj.mannam.domain.survey.repository;

import com.kslj.mannam.domain.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    SurveyQuestion getSurveyQuestionById(Long id);

    List<SurveyQuestion> getSurveyQuestionBySurveySession_Id(Long surveySessionId);
}
