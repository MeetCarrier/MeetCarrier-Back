package com.kslj.mannam.domain.meeting.entity;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.meeting.enums.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String location;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int updateCount = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    public void updateNote(String note) {
        this.note = note;
    }

    // 제안 수락 -> 일정 확정
    public void confirm() {
        if (this.status != MeetingStatus.PENDING) {
            throw new IllegalStateException("Meeting has already been confirmed");
        }
        this.status = MeetingStatus.ACCEPTED;
    }

    // 제안 거절
    public void reject() {
        if (this.status != MeetingStatus.REJECTED) {
            throw new IllegalStateException("Meeting has already been rejected");
        }
        this.status = MeetingStatus.REJECTED;
    }

    // 확정된 일정 수정 (최대 3회)
    public void updateSchedule(LocalDateTime date, String location) {
        if (this.status != MeetingStatus.ACCEPTED)
            throw new IllegalStateException("확정된 일정만 수정할 수 있습니다.");

        if (this.updateCount >= 3) {
            throw new IllegalStateException("일정은 최대 3회까지 수정할 수 있습니다.");
        }

        this.date = date;
        this.location = location;
        this.updateCount++;
    }
}
