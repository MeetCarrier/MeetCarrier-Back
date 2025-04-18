package com.kslj.mannam.domain.review.controller;

import com.kslj.mannam.domain.review.dto.ReviewRequestDto;
import com.kslj.mannam.domain.review.dto.ReviewResponseDto;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/review/{userId}")
    public ResponseEntity<?> getReviews(@PathVariable(value = "userId") long userId) {
        List<ReviewResponseDto> reviews = reviewService.getReview(userId);

        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/review/{userId}/submit")
    public ResponseEntity<?> createReview(@PathVariable(value = "userId") long userId,
                                          @RequestBody ReviewRequestDto requestDto,
                                          UserDetailsImpl userDetails) {
        long reviewId = reviewService.createReview(userId, requestDto, userDetails.getUser());

        return ResponseEntity.ok(reviewId);
    }

    @PatchMapping("/review/{reviewId}/update")
    public ResponseEntity<?> updateReview(@PathVariable(value = "reviewId") long reviewId,
                                          @RequestBody ReviewRequestDto requestDto,
                                          UserDetailsImpl userDetails) {
        reviewService.updateReview(reviewId, requestDto, userDetails.getUser());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/review/{reviewId}/delete")
    public ResponseEntity<?> deleteReview(@PathVariable(value = "reviewId") long reviewId) {
        reviewService.deleteReview(reviewId);

        return ResponseEntity.ok().build();
    }
}
