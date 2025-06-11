package com.kslj.mannam.domain.report.entity;

import com.kslj.mannam.domain.report.enums.ReportStatus;
import com.kslj.mannam.domain.report.enums.ReportType;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.Registered;

    @Column(name = "report_content", nullable = false)
    private String content;

    @Column(name = "report_description")
    private String description;

    @Builder.Default
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Setter
    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReportReply reply;

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
}
