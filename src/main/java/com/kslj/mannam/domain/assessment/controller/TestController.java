package com.kslj.mannam.domain.assessment.controller;

import com.kslj.mannam.domain.assessment.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller("/tests")
class TestController {

    private final TestService testService;

//    @GetMapping("/")
//    public ResponseEntity<Object> index() {
//    }
}
