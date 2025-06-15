package com.kslj.mannam.domain.review.controller;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.service.MatchService;
import com.kslj.mannam.domain.review.dto.ReviewByReviewerIdDto;
import com.kslj.mannam.domain.review.dto.ReviewRequestDto;
import com.kslj.mannam.domain.review.dto.ReviewResponseDto;
import com.kslj.mannam.domain.review.service.ReviewService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name="리뷰", description="리뷰 관리 API")
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final MatchService matchService;
    private final UserService userService;

    @Operation(
            summary = "내가 받은 리뷰 조회",
            description = "로그인한 사용자가 받은 모든 리뷰 목록을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = ReviewResponseDto.class)
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "204", description = "리뷰가 존재하지 않음")
            }
    )
    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.inspectUserDetails(userDetails);
        List<ReviewResponseDto> reviews = reviewService.getReview(userDetails.getId());

        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "내가 남긴 리뷰 조회",
            description = "로그인한 사용자가 작성한 리뷰 목록을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = ReviewByReviewerIdDto.class)
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "204", description = "작성한 리뷰 없음")
            }
    )
    @GetMapping("/written")
    public ResponseEntity<List<ReviewByReviewerIdDto>> getWrittenReviews(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.inspectUserDetails(userDetails);
        List<ReviewByReviewerIdDto> writtenReviews = reviewService.getReviewByReviewerId(userDetails.getId());

        if (writtenReviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(writtenReviews);
    }

    @GetMapping("/{userId}")
    @Operation(
            summary     = "리뷰 조회",
            description = "특정 유저가 받은 리뷰들을 조회합니다.",
            parameters = {
                    @Parameter(
                            name        = "userId",
                            description = "조회할 유저의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = ReviewResponseDto.class)
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "204", description = "리뷰가 존재하지 않음")
            }
    )
    public ResponseEntity<?> getReviewsByUserId(@PathVariable("userId") long userId) {
        List<ReviewResponseDto> reviews = reviewService.getReview(userId);

        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{userId}")
    @Operation(
            summary     = "리뷰 등록",
            description = "특정 유저에 대한 리뷰를 등록합니다. step은 1,2,3의 값을 가지며, 매칭 목록을 불러왔을 때 매칭의 status값에 따라 달라집니다.<br>" +
                    "- Survey_Cancelled -> 1<br>" +
                    "- Chat_Cancelled -> 2<br>" +
                    "- Reviewing -> 3",
            parameters = {
                    @Parameter(
                            name        = "userId",
                            description = "리뷰가 등록될 유저의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리뷰 등록 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ReviewRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description  = "생성 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema    = @Schema(type = "integer", format = "int64"),
                                    examples  = @ExampleObject(value = "42")
                            )
                    )
            }
    )
    public ResponseEntity<?> createReview(@PathVariable("userId") long userId,
                                          @RequestBody ReviewRequestDto requestDto,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.inspectUserDetails(userDetails);
        Match match = matchService.findExistingMatch(userId, userDetails.getId());

        long reviewId = reviewService.createReview(userId, requestDto, userDetails.getUser());

        boolean hasOtherUserReviewed = reviewService.hasUserReview(userId, userDetails.getId());

        if (hasOtherUserReviewed) {
            matchService.updateMatchStatus(match.getId(), MatchStatus.Completed);
        }

        return ResponseEntity.ok(reviewId);
    }

    @PatchMapping("/{reviewId}")
    @Operation(
            summary     = "리뷰 수정",
            description = "지정된 ID의 리뷰를 수정합니다.\n요청 전송 시 필요한 부분의 데이터만 채워서 보내면 됩니다.",
            parameters = {
                    @Parameter(
                            name        = "reviewId",
                            description = "수정할 리뷰의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리뷰 수정 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ReviewRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "수정 성공"
                    )
            }
    )
    public ResponseEntity<Void> updateReview(
            @PathVariable("reviewId") long reviewId,
            @RequestBody ReviewRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.inspectUserDetails(userDetails);
        reviewService.updateReview(reviewId, requestDto, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
            summary     = "리뷰 삭제",
            description = "지정된 ID의 리뷰를 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "reviewId",
                            description = "삭제할 리뷰의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "삭제 성공"
                    )
            }
    )
    public ResponseEntity<Void> deleteReview(
            @PathVariable("reviewId") long reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.inspectUserDetails(userDetails);
        reviewService.deleteReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
