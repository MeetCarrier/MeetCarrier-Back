package com.kslj.mannam.domain.user.entity;

import com.kslj.mannam.domain.user.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_action_log")
public class UserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "action_type")
    private ActionType actionType;

    @Column(nullable = false, name = "action_date")
    private LocalDate actionDate;

    @Column(nullable = false, name = "action_score")
    private double actionScore;
}
