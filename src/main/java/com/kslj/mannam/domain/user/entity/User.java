package com.kslj.mannam.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kslj.mannam.domain.user.dto.UserSignUpRequestDto;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.Role;
import com.kslj.mannam.domain.user.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

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

    private Double latitude;
    private Double longitude;

    private String interests;

    @Builder.Default
    @Column(nullable = false)
    private double footprint = 36.5;

    @Column(nullable = false)
    private Long age;

    private String question;
    
    @Column(name = "question_list")
    private String questionList;

    @Column(name = "img_url")
    private String imgUrl;

    private String phone;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    @JsonProperty("deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @Column(name = "max_age_gap", nullable = false)
    private int maxAgeGap = 5;

    @Builder.Default
    @Column(name = "allow_opposite_gender", nullable = false)
    private boolean allowOppositeGender = false;

    @Builder.Default
    @Column(name = "max_matching_distance", nullable = false)
    private double maxMatchingDistance = 10;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id); // id 필드를 기준으로 비교
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // id 필드를 기준으로 해시코드 생성
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void updateLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void updateLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void updateAge(Long age) {
        this.age = age;
    }

    public void updateInterests(String interests) {
        this.interests = interests;
    }

    public void updateQuestion(String question) {
        this.question = question;
    }

    public void updateQuestionList(String questionList) {
        this.questionList = questionList;
    }

    public void updateImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateMaxAgeGap(int maxAgeGap) {
        this.maxAgeGap = maxAgeGap;
    }

    public void updateAllowOppositeGender(boolean allowOppositeGender) {
        this.allowOppositeGender = allowOppositeGender;
    }

    public void updateMaxMatchingDistance(double maxMatchingDistance) {
        this.maxMatchingDistance = maxMatchingDistance;
    }

    public void updateFootprint(double footprint) {
        this.footprint = footprint;
    }

    public void withdraw() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void rejoin(UserSignUpRequestDto dto) {
        this.nickname = dto.getNickname();
        this.gender = dto.getGender();
        this.age = dto.getAge();
        this.interests = null;
        this.footprint = 36.5f;
        this.maxAgeGap = 5;
        this.allowOppositeGender = false;
        this.maxMatchingDistance = 10;
        this.isDeleted = false;
        this.deletedAt = null;
        this.phone = null;
        this.latitude = null;
        this.longitude = null;
    }
}
