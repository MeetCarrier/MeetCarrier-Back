package com.kslj.mannam.sms;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Tag(name = "전화번호 인증 API", description = "전화번호 인증 요청 및 검증 API")
public class smsController {

    private final smsService smsService;

    @GetMapping("/api/test-sms")
    @Operation(
            summary = "SMS 인증 테스트 페이지",
            description = "SMS 인증 테스트를 위한 HTML 페이지를 반환합니다."
    )
    public String testSmsPage() {
        return "test-sms";
    }

    @PostMapping("/api/send-sms")
    @ResponseBody
    @Operation(
            summary = "인증번호 전송",
            description = "사용자의 전화번호로 인증번호를 전송합니다.",
            parameters = {
                    @Parameter(name = "userPhone", in = ParameterIn.QUERY, required = true, description = "휴대폰 번호 (숫자만, 예: 01012345678)")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증번호 전송 성공", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    public ResponseEntity<?> sendOne(@RequestParam("userPhone") String userPhone) {
        smsService.sendSmsCode(userPhone);
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    @PostMapping("/api/verify-sms")
    @Operation(
            summary = "인증번호 확인",
            description = "입력된 인증번호를 확인하여 일치 여부를 반환합니다.",
            parameters = {
                    @Parameter(name = "userPhone", in = ParameterIn.QUERY, required = true, description = "휴대폰 번호"),
                    @Parameter(name = "smsCode", in = ParameterIn.QUERY, required = true, description = "사용자가 입력한 인증번호")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공"),
                    @ApiResponse(responseCode = "400", description = "인증번호가 만료되었거나 존재하지 않음"),
                    @ApiResponse(responseCode = "401", description = "인증 실패: 코드 불일치"),
                    @ApiResponse(responseCode = "500", description = "알 수 없는 서버 오류")
            }
    )
    public ResponseEntity<?> verifySms(
            @RequestParam("userPhone") String userPhone,
            @RequestParam("smsCode") String smsCode) {
        String result = smsService.verifySmsCode(userPhone, smsCode);

        return switch (result) {
            case "NOT_REQUESTED_OR_EXPIRED" ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 만료되었거나 존재하지 않습니다.");
            case "SUCCESS" ->
                    ResponseEntity.ok("인증 성공");
            case "FAIL" ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 코드가 일치하지 않습니다.");
            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류가 발생했습니다.");
        };
    }
}