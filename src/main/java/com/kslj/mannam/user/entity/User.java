package com.kslj.mannam.user.entity;

import com.kslj.mannam.block.entity.Block;
import com.kslj.mannam.notification.entity.Notification;
import com.kslj.mannam.user.enums.Gender;
import com.kslj.mannam.user.enums.SocialType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
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
}
