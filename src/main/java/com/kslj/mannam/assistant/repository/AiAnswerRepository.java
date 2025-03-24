package com.kslj.mannam.assistant.repository;

import com.kslj.mannam.assistant.entity.AiAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiAnswerRepository extends JpaRepository<AiAnswer, Long> {
}
