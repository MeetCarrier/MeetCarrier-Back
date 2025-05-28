package com.kslj.mannam.domain.notification.controller;

import com.kslj.mannam.domain.notification.dto.NotificationResponseDto;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name="알림", description="알림 조회 및 삭제 API")
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    // 알림 조회
    @Operation(
            summary     = "알림 조회",
            description = "로그인한 사용자의 모든 알림 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array        = @ArraySchema(schema = @Schema(implementation = NotificationResponseDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            }
    )
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<NotificationResponseDto> notifications = notificationService.getNotifications(userDetails.getUser());

        return ResponseEntity.ok(notifications);
    }

    // 알림 삭제 (단일)
    @Operation(
            summary     = "알림 삭제 (단일)",
            description = "특정 알림 ID에 해당하는 알림을 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "notificationId",
                            description = "삭제할 알림의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공 (Content 없음)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            }
    )
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("notificationId") Long notificationId
    ) {
        notificationService.deleteNotification(userDetails.getUser(), notificationId);

        return ResponseEntity.noContent().build();
    }

    // 알림 삭제 (일괄)
    @Operation(
            summary     = "알림 삭제 (일괄)",
            description = "로그인한 사용자의 모든 알림을 일괄 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "일괄 삭제 성공 (Content 없음)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            }
    )
    @DeleteMapping
    public ResponseEntity<?> deleteAllNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.deleteAllNotifications(userDetails.getUser());

        return ResponseEntity.noContent().build();
    }

}
