package com.kslj.mannam.controller;

import com.kslj.mannam.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor
@Controller
public class TestController {
    private final TestService testService;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        testService.addHello();
        return "hello";
    }
}
