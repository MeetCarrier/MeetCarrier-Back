package com.kslj.mannam.domain.assistant.repository;

import com.kslj.mannam.domain.assistant.entity.AssistantQuestion;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantQuestionRepository extends JpaRepository<AssistantQuestion, Long> {
    List<AssistantQuestion> findAllByUser(User user);
}
