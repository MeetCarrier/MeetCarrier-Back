package com.kslj.mannam.domain.review.controller;

import com.kslj.mannam.domain.review.dto.ReviewRequestDto;
import com.kslj.mannam.domain.review.dto.ReviewResponseDto;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getReviews(@PathVariable(value = "userId") long userId) {
        List<ReviewResponseDto> reviews = reviewService.getReview(userId);

        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> createReview(@PathVariable(value = "userId") long userId,
                                          @RequestBody ReviewRequestDto requestDto,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        long reviewId = reviewService.createReview(userId, requestDto, userService.getUserById(1));

        return ResponseEntity.ok(reviewId);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable(value = "reviewId") long reviewId,
                                          @RequestBody ReviewRequestDto requestDto,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        reviewService.updateReview(reviewId, requestDto, userService.getUserById(1));

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable(value = "reviewId") long reviewId) {
        reviewService.deleteReview(reviewId);

        return ResponseEntity.ok().build();
    }
}
