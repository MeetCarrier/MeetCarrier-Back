package com.kslj.mannam.domain.assistant.repository;

import com.kslj.mannam.domain.assistant.entity.AssistantAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistantAnswerRepository extends JpaRepository<AssistantAnswer, Long> {
}
