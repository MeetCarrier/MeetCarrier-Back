package com.kslj.mannam.sms;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Tag(name="전화번호 인증 API", description="전화번호 인증 요청 및 검증 API")
public class smsController {

    private final smsService smsService;

    @GetMapping("/api/test-sms")
    public String testSmsPage() {
        return "test-sms";
    }

    // 인증 코드 전송
    @PostMapping("/api/send-sms")
    @ResponseBody
    public ResponseEntity<?> sendOne(@RequestParam("userPhone") String userPhone) {
        smsService.sendSmsCode(userPhone);
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    // 인증 코드 검사
    @PostMapping("/api/verify-sms")
    public ResponseEntity<?> verifySms(
            @RequestParam("userPhone") String userPhone,
            @RequestParam("smsCode") String smsCode) {
        String result = smsService.verifySmsCode(userPhone, smsCode);

        return switch (result) {
            case "NOT_REQUESTED_OR_EXPIRED" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 만료되었거나 존재하지 않습니다.");
            case "SUCCESS" -> ResponseEntity.ok("인증 성공");
            case "FAIL" -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 코드가 일치하지 않습니다.");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류가 발생했습니다.");
        };
    }
}
