package com.kslj.mannam.domain.survey.entity;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.survey.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "survey_sessions")
public class SurveySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.Ongoing;

    @Column(name = "answered_count", nullable = false)
    private int answeredCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    public void incrementAnsweredCount() {
        ++answeredCount;
    }

    public void updateSessionStatus(SessionStatus newStatus) {
        this.status = newStatus;
    }
}
