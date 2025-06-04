package com.kslj.mannam.domain.room.entity;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.room.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.Activate;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "deactivation_time", nullable = false)
    private LocalDateTime deactivationTime = LocalDateTime.now().plusHours(24);

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    public void updateStatus(RoomStatus newStatus) {
        this.status = newStatus;
    }

    public void updateDeactivationTime(LocalDateTime newDeactivationTime) {
        this.deactivationTime = newDeactivationTime;
    }
}
