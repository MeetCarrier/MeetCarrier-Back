package com.kslj.mannam.sms;

import com.kslj.mannam.domain.user.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class smsController {

    private DefaultMessageService messageService;
    private final UserService userService;

    @Value("${smsApiKey}")
    private String apiKey;

    @Value("${smsApiSecret}")
    private String apiSecret;

    public smsController(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @GetMapping("/api/test-sms")
    public String testSmsPage() {
        return "test-sms";
    }

    // 인증 코드 전송
    @PostMapping("/api/send-sms")
    @ResponseBody
    public ResponseEntity<?> sendOne(@RequestParam("userPhone") String userPhone, HttpSession session) {
        String smsCode = userService.getSmsCode();
        session.setAttribute("smsCode", smsCode);
        session.setAttribute("smsCodeTime", System.currentTimeMillis());

        Message message = new Message();
        message.setFrom("01028714525");
        message.setTo(userPhone);
        message.setText("[만남배달부] 인증번호 [ " + smsCode + " ]를 입력해주세요.");

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    // 인증 코드 검사
    @PostMapping("/api/verify-sms")
    public ResponseEntity<?> verifySms(@RequestParam("smsCode") String smsCode, HttpSession session) {
        String savedCode = (String) session.getAttribute("smsCode");
        Long savedTime = (Long) session.getAttribute("smsCodeTime");

        if (savedCode == null || savedTime == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 요청되지 않았습니다.");
        }

        // 5분간 유효
        long elapsedTime = System.currentTimeMillis() - savedTime;
        if (elapsedTime > 5 * 60 * 1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 만료되었습니다.");
        }

        if (smsCode.equals(savedCode)) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 코드가 일치하지 않습니다.");
        }
    }
}
