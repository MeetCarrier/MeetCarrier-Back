package com.kslj.mannam.assistant.repository;

import com.kslj.mannam.assistant.entity.AiInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiInquiryRepository extends JpaRepository<AiInquiry, Long> {
}
