package com.kslj.mannam.domain.assistant.repository;

import com.kslj.mannam.domain.assistant.entity.AiAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiAnswerRepository extends JpaRepository<AiAnswer, Long> {
}
