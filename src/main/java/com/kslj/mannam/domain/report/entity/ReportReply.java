package com.kslj.mannam.domain.report.entity;

import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report_replies")
public class ReportReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "replied_at", nullable = false)
    private LocalDateTime repliedAt = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replier_id", nullable = false)
    private User user;

    public void updateContent(String content) {
        this.content = content;
    }

    public void unlinkReport() {
        if (this.report != null) {
            this.report.setReply(null);
            this.report = null;
        }
    }
}
