package com.kslj.mannam.review;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.review.dto.ReviewRequestDto;
import com.kslj.mannam.domain.review.dto.ReviewResponseDto;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.domain.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class ReviewTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TestUtils testUtils;

    // reviewRequestDto 생성 메서드
    private ReviewRequestDto createReviewRequestDto(int rating, String content) {
        return ReviewRequestDto.builder()
                .rating(rating)
                .content(content)
                .build();
    }

    // 리뷰 생성 및 조회 테스트
    @Test
    public void createReviewTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        User testReviewer = testUtils.createAndGetTestUser();
        ReviewRequestDto requestDto = createReviewRequestDto(5, "활발해요, 마음이 잘 맞아요");

        // when
        reviewService.createReview(testUser.getId(), requestDto, testReviewer);
        List<ReviewResponseDto> reviews = reviewService.getReview(testUser.getId());

        // then
        Assertions.assertEquals(1, reviews.size());
        Assertions.assertEquals(5, reviews.get(0).getRating());
        Assertions.assertEquals("활발해요, 마음이 잘 맞아요", reviews.get(0).getContent());
    }

    // 리뷰 수정 테스트
    @Test
    public void updateReviewTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        User testReviewer = testUtils.createAndGetTestUser();
        ReviewRequestDto requestDto = createReviewRequestDto(5, "활발해요, 마음이 잘 맞아요");
        long reviewId = reviewService.createReview(testUser.getId(), requestDto, testReviewer);

        // when
        ReviewRequestDto updateRequestDto = createReviewRequestDto(3, "보통이예요, 편해요");
        reviewService.updateReview(reviewId, updateRequestDto, testReviewer);
        List<ReviewResponseDto> reviews = reviewService.getReview(testUser.getId());

        // then
        Assertions.assertEquals(1, reviews.size());
        Assertions.assertEquals(3, reviews.get(0).getRating());
        Assertions.assertEquals("보통이예요, 편해요", reviews.get(0).getContent());
    }

    // 리뷰 삭제 테스트
    @Test
    public void deleteReviewTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        User testReviewer1 = testUtils.createAndGetTestUser();
        User testReviewer2 = testUtils.createAndGetTestUser();
        User testReviewer3 = testUtils.createAndGetTestUser();
        ReviewRequestDto requestDto1 = createReviewRequestDto(5, "활발해요, 마음이 잘 맞아요");
        ReviewRequestDto requestDto2 = createReviewRequestDto(4, "분위기가 좋아요");
        ReviewRequestDto requestDto3 = createReviewRequestDto(5, "친절해요, 재밌어요");
        long reviewId1 = reviewService.createReview(testUser.getId(), requestDto1, testReviewer1);
        long reviewId2 = reviewService.createReview(testUser.getId(), requestDto2, testReviewer2);
        long reviewId3 = reviewService.createReview(testUser.getId(), requestDto3, testReviewer3);

        // when
        reviewService.deleteReview(reviewId1, testUser);

        // then
        Assertions.assertEquals(2, reviewService.getReview(testUser.getId()).size());
    }
}
