package com.kslj.mannam.domain.match.entity;

import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.Matched;

    @Column(nullable = false)
    private double score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name="user1_entered", nullable = false)
    @Builder.Default
    private boolean user1Entered = false;

    @Column(name="user2_entered", nullable = false)
    @Builder.Default
    private boolean user2Entered = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @Column(name = "cancel_reason_code", columnDefinition = "TEXT")
    private String cancelReasonCode;

    @Column(name = "cancel_custom_reason", columnDefinition = "TEXT")
    private String cancelCustomReason;

    public void updateStatus(MatchStatus status) {
        this.status = status;
    }
    public boolean hasUser(User sender) {
        return user1.equals(sender) || user2.equals(sender);
    }

    public void markUserEntered(User user) {
        if (user1.equals(user)) this.user1Entered = true;
        else if (user2.equals(user)) this.user2Entered = true;
    }

    public boolean isEntered(User user) {
        return user1.equals(user) ? user1Entered : user2Entered;
    }

    public User getOtherUser(User user) {
        if (user1.equals(user)) return user2;
        else if (user2.equals(user)) return user1;
        return user;
    }

    // 중단 처리 메서드
    public void cancelMatch(User canceller, MatchStatus cancelStatus, String reasonCodes, String customReason) {
        if (cancelStatus != MatchStatus.Survey_Cancelled && cancelStatus != MatchStatus.Chat_Cancelled) {
            throw new IllegalArgumentException("중단 상태는 Survey_Cancelled 또는 Chat_Cancelled만 가능합니다.");
        }
        if (!hasUser(canceller)) {
            throw new IllegalArgumentException("이 매칭의 참여자가 아닙니다.");
        }
        if (reasonCodes == null || reasonCodes.isBlank()) {
            throw new IllegalArgumentException("중단 사유를 입력해야 합니다.");
        }
        this.status = cancelStatus;
        this.cancelledBy = canceller;
        this.cancelReasonCode = reasonCodes;
        this.cancelCustomReason = customReason;
    }
}
