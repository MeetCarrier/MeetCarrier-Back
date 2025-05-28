package com.kslj.mannam.websocket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebSocketTestController {

    @GetMapping("/api/websocket-test")
    public String websocketTestPage() {
        return "websocket-test"; // templates/websocket-test.html을 반환
    }
}