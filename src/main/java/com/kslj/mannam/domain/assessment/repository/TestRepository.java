package com.kslj.mannam.domain.assessment.repository;

import com.kslj.mannam.domain.assessment.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
}
