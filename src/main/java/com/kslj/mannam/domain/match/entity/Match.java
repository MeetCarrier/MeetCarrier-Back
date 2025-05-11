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

    @Column(nullable = false)
    @Builder.Default
    private boolean user1Entered = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean user2Entered = false;

    public void updateStatus(MatchStatus status) {
        this.status = status;
    }
    public boolean hasUser(User sender) {
        return user1.equals(sender) || user2.equals(sender);
    }

    public void markUserEntered(User user) {
        if (user.equals(user1)) this.user1Entered = true;
        else if (user.equals(user2)) this.user2Entered = true;
    }

    public boolean isEntered(User user) {
        return user.equals(user1) ? user1Entered : user2Entered;
    }
}
