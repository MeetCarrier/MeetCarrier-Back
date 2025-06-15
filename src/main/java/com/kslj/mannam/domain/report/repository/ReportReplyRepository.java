package com.kslj.mannam.domain.report.repository;

import com.kslj.mannam.domain.report.entity.Report;
import com.kslj.mannam.domain.report.entity.ReportReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportReplyRepository extends JpaRepository<ReportReply, Long> {
    Optional<ReportReply> findByReport(Report report);
}
