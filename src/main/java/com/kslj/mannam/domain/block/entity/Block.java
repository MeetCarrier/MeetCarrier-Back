package com.kslj.mannam.domain.block.entity;

import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blocks")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차단한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 차단할 전화번호
    @Column(name = "blocked_phone", nullable = false)
    private String blockedPhone;

    // 전화번호 설명
    @Column(name = "blocked_info")
    private String blockedInfo;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public void updateInfo(String blockedInfo) {
        this.blockedInfo = blockedInfo;
    }

    public void updateBlockedPhone(String blockedPhone) {
        this.blockedPhone = blockedPhone;
    }
}
