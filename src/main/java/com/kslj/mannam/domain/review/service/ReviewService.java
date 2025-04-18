package com.kslj.mannam.domain.review.service;

import com.kslj.mannam.domain.review.dto.ReviewRequestDto;
import com.kslj.mannam.domain.review.dto.ReviewResponseDto;
import com.kslj.mannam.domain.review.entity.Review;
import com.kslj.mannam.domain.review.repository.ReviewRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;

    // 리뷰 등록
    public long createReview(long userId, ReviewRequestDto requestDto, User reviewer) {
        User targetUser = userService.getUserById(userId);

        Review newReview = Review.builder()
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .user(targetUser)
                .reviewer(reviewer)
                .build();

        Review savedReview = reviewRepository.save(newReview);

        return savedReview.getId();
    }

    // 리뷰 조회
    public List<ReviewResponseDto> getReview(long userId) {
        List<ReviewResponseDto> reviewDtos = new ArrayList<>();

        User user = userService.getUserById(userId);
        List<Review> reviews = reviewRepository.findReviewByUser(user);

        for(Review review : reviews) {
            ReviewResponseDto reviewDto = ReviewResponseDto.builder()
                    .reviewId(review.getId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .createdAt(review.getCreatedAt())
                    .reviewer(review.getReviewer())
                    .build();

            reviewDtos.add(reviewDto);
        }

        return reviewDtos;
    }

    // 리뷰 수정
    public long updateReview(long reviewId, ReviewRequestDto requestDto, User reviewer) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다. reviewId = " + reviewId));

        if(review.getReviewer().equals(reviewer)) {
            review.updateRatingAndContent(requestDto.getRating(), requestDto.getContent());
        }
        else {
            throw new RuntimeException("작성자가 일치하지 않습니다.");
        }

        return reviewId;
    }

    // 리뷰 삭제
    public void deleteReview(long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
