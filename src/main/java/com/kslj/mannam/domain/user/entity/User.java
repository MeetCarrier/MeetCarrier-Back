package com.kslj.mannam.domain.user.entity;

import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.notification.entity.Notification;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "social_id")
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "social_type")
    private SocialType socialType;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String personalities;

    @Column(nullable = false)
    private String preferences;

    @Column(nullable = false)
    private String interests;

    @Column(nullable = false)
    private float footprint;

    private String questions;

    @Column(name = "img_url")
    private String imgUrl;

    private String phone;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<Block> blocks;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<Notification> notifications;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
