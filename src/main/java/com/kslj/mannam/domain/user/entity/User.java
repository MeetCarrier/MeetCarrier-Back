package com.kslj.mannam.domain.user.entity;

import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.notification.entity.Notification;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private String interests;

    @Column(nullable = false)
    private float footprint;

    @Column(nullable = false)
    private Long age;

    private String questions;

    @Column(name = "img_url")
    private String imgUrl;

    private String phone;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void updateRegion(String region) {
        this.region = region;
    }

    public void updateAge(Long age) {
        this.age = age;
    }

    public void updatePersonalities(String personalities) {
        this.personalities = personalities;
    }

    public void updateInterests(String interests) {
        this.interests = interests;
    }

    public void updateQuestions(String questions) {
        this.questions = questions;
    }

    public void updateImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void withdraw() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void rejoin(UserSignUpRequestDto dto) {
        this.nickname = dto.getNickname();
        this.region = dto.getRegion();
        this.gender = dto.getGender();
        this.age = dto.getAge();
        this.personalities = dto.getPersonalities();
        this.interests = dto.getInterests();
        this.footprint = 36.5f;
        this.isDeleted = false;
        this.deletedAt = null;
    }
}
