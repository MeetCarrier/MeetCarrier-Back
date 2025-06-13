package com.kslj.mannam.domain.review.service;

import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.review.dto.ReviewByReviewerIdDto;
import com.kslj.mannam.domain.review.dto.ReviewRequestDto;
import com.kslj.mannam.domain.review.dto.ReviewResponseDto;
import com.kslj.mannam.domain.review.entity.Review;
import com.kslj.mannam.domain.review.repository.ReviewRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserActionLogService;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final UserActionLogService userActionLogService;
    private final NotificationService notificationService;

    // 리뷰 등록
    @Transactional
    public long createReview(long userId, ReviewRequestDto requestDto, User reviewer) {
        User targetUser = userService.getUserById(userId);

        Review newReview = Review.builder()
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .step(requestDto.getStep())
                .user(targetUser)
                .reviewer(reviewer)
                .build();

        Review savedReview = reviewRepository.save(newReview);
        userActionLogService.logUserReview(targetUser, newReview);
        notificationService.createNotification(NotificationType.Review, targetUser, savedReview.getId());

        return savedReview.getId();
    }

    // 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReview(long userId) {
        List<ReviewResponseDto> reviewDtos = new ArrayList<>();

        User user = userService.getUserById(userId);
        List<Review> reviews = reviewRepository.findReviewByUser(user);

        for(Review review : reviews) {
            ReviewResponseDto reviewDto = ReviewResponseDto.builder()
                    .reviewId(review.getId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .step(review.getStep())
                    .createdAt(review.getCreatedAt())
                    .reviewerId(review.getReviewer().getId())
                    .reviewerName(review.getReviewer().getNickname())
                    .build();

            reviewDtos.add(reviewDto);
        }

        return reviewDtos;
    }

    // 작성자 기준으로 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewByReviewerIdDto> getReviewByReviewerId(long userId) {
        List<ReviewByReviewerIdDto> reviewDtos = new ArrayList<>();

        List<Review> reviews = reviewRepository.findReviewByReviewerId(userId);

        for(Review review : reviews) {
            ReviewByReviewerIdDto reviewDto = ReviewByReviewerIdDto.builder()
                    .reviewId(review.getId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .step(review.getStep())
                    .createdAt(review.getCreatedAt())
                    .userId(review.getUser().getId())
                    .userName(review.getUser().getNickname())
                    .build();

            reviewDtos.add(reviewDto);
        }

        return reviewDtos;
    }

    // 리뷰 수정
    @Transactional
    public long updateReview(long reviewId, ReviewRequestDto requestDto, User reviewer) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. reviewId = " + reviewId));

        if(review.getReviewer().equals(reviewer)) {
            if (requestDto.getContent() != null) review.updateContent(requestDto.getContent());
            if (requestDto.getRating() != null) review.updateRating(requestDto.getRating());
        }
        else {
            throw new IllegalStateException("작성자가 일치하지 않습니다.");
        }

        return reviewId;
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    // 유저 Id로 등록된 리뷰 조회
    @Transactional(readOnly = true)
    public boolean hasUserReview(long reviewerId, long targetUserId) {
        return reviewRepository.existsByReviewer_IdAndUser_Id(reviewerId, targetUserId);
    }
}
