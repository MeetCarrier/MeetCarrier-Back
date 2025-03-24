package com.kslj.mannam.block.entity;

import com.kslj.mannam.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
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

    // 차단당한 사용자
    @Column(name = "blocked_phone", nullable = false)
    private String blockedPhone;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
