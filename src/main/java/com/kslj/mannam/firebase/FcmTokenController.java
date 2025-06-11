package com.kslj.mannam.firebase;

import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM 토큰 API", description = "사용자의 FCM 토큰을 등록하거나 갱신합니다.")
@Controller
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @Operation(
            summary = "FCM 토큰 저장 또는 갱신",
            description = "로그인한 사용자의 FCM 토큰을 저장하거나 기존 토큰을 갱신합니다."
    )
    @ApiResponse(responseCode = "200", description = "FCM 토큰 저장 또는 갱신 성공")
    @PostMapping("/token")
    public ResponseEntity<Void> saveFcmToken(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody FcmTokenRequestDto request
    ) {
        System.out.println("request = " + request);
        fcmTokenService.saveOrUpdateToken(userDetails.getId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public String fcmTestPage() {
        return "fcm-test";
    }

    @PostMapping("/test")
    public ResponseEntity<Void> sendTestPush(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        fcmTokenService.sendPushToUserAsync(
                userDetails.getUser(),
                "FCM 테스트 알림",
                "푸시 알림이 정상적으로 전송되었습니다.",
                "/",
                null
        );
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "FCM 토큰 삭제",
            description = "클라이언트에서 로그아웃 시 FCM 토큰을 서버에서도 제거합니다. " +
                    "이 요청은 로그아웃 시 클라이언트가 토큰을 삭제하는 것과 함께 호출되어야 합니다."
    )
    @ApiResponse(responseCode = "200", description = "FCM 토큰 삭제 성공")
    @DeleteMapping("/token")
    public ResponseEntity<Void> deleteFcmToken(@RequestBody FcmTokenRequestDto request) {
        fcmTokenService.deleteToken(request.getToken());
        return ResponseEntity.ok().build();
    }
}
