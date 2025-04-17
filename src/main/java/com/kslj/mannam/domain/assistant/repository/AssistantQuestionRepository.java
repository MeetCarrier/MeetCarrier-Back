package com.kslj.mannam.domain.assistant.repository;

import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantQuestionRepository extends JpaRepository<AssistantQuestion, Long> {
    @Query("select q from AssistantQuestion q left join fetch q.answer where q.user.id = :userId")
    List<AssistantQuestion> findAllWithAnswerByUserId(@Param("userId") long userId);
}
