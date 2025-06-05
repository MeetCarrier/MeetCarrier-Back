package com.kslj.mannam.sms;

import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.redis.RedisUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class smsService {

    private final UserService userService;
    private final RedisUtils redisUtils;

    private DefaultMessageService messageService;

    @Value("${smsApiKey}")
    private String apiKey;

    @Value("${smsApiSecret}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    // 인증 코드 전송 및 Redis 저장
    public void sendSmsCode(String userPhone) {
        String smsCode = userService.getSmsCode();

        // Redis에 5분 동안 저장
        redisUtils.setData("sms:" + userPhone, smsCode, 300);

        Message message = new Message();
        message.setFrom("01028714525");
        message.setTo(userPhone);
        message.setText("[만남배달부] 인증번호 [ " + smsCode + " ]를 입력해주세요.");

        this.messageService.sendOne(new SingleMessageSendingRequest(message));
    }

    // Redis에서 인증번호 조회 및 검증
    public String verifySmsCode(String userPhone, String inputCode) {
        String savedCode = redisUtils.getData("sms:" + userPhone);

        if (savedCode == null) {
            return "NOT_REQUESTED_OR_EXPIRED";
        }

        if (inputCode.equals(savedCode)) {
            redisUtils.deleteData("sms:" + userPhone);
            return "SUCCESS";
        } else {
            return "FAIL";
        }
    }
}