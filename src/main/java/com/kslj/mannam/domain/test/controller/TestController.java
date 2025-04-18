package com.kslj.mannam.domain.test.controller;

import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.entity.Test;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
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
class TestController {

    private final TestService testService;

    // 현재 유저의 테스트 결과 목록 반환
    @GetMapping("/tests/")
    public ResponseEntity<?> getTestList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Test> tests = testService.getTestByUserId(userDetails.getUser());

        return ResponseEntity.ok(tests);
    }

    // 새로운 테스트 결과 추가
    @PostMapping("/tests/register")
    public ResponseEntity<?> createTest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TestRequestDto requestDto) {
        long savedTestId = testService.createTest(requestDto, userDetails.getUser());

        return ResponseEntity.ok("테스트 결과가 추가되었습니다. TestId = " + savedTestId);
    }

    // 테스트 결과 삭제
    @DeleteMapping("/tests/{testId}")
    public ResponseEntity<?> deleteTest(@PathVariable(value = "testId") Long testId) {
        long deletedTestId = testService.deleteTestByTestId(testId);

        return ResponseEntity.ok("테스트 결과가 삭제되었습니다. TestId = " + deletedTestId);
    }
}
