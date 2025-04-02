package com.kslj.mannam.domain.test.repository;

import com.kslj.mannam.domain.test.entity.Test;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    public List<Test> findTop10ByUserOrderByCreatedAtDesc(User user);
}
