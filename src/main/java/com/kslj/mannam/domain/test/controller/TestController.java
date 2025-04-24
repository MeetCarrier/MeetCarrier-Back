package com.kslj.mannam.domain.test.controller;

import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.domain.user.service.UserService;
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
@RequestMapping("/test")
class TestController {

    private final TestService testService;
    private final UserService userService;

    // 현재 유저의 테스트 결과 목록 반환
    @GetMapping
    public ResponseEntity<?> getTestList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TestResponseDto> tests = testService.getTestByUserId(userService.getUserById(1));

        return ResponseEntity.ok(tests);
    }

    // 새로운 테스트 결과 추가
    @PostMapping("/register")
    public ResponseEntity<?> createTest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TestRequestDto requestDto) {
        long savedTestId = testService.createTest(requestDto, userService.getUserById(1));

        return ResponseEntity.ok("테스트 결과가 추가되었습니다. TestId = " + savedTestId);
    }

    // 테스트 결과 삭제
    @DeleteMapping("/{testId}")
    public ResponseEntity<?> deleteTest(@PathVariable(value = "testId") Long testId) {
        long deletedTestId = testService.deleteTestByTestId(testId);

        return ResponseEntity.ok("테스트 결과가 삭제되었습니다. TestId = " + deletedTestId);
    }
}
