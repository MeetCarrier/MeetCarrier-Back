package com.kslj.mannam.domain.report.repository;

import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByReporter(User reporter);
}
