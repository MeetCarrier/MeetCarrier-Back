package com.kslj.mannam.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-base64}")
    private String firebaseServiceAccountBase64;

    @PostConstruct
    public void init() {
        try {
            // 설정 데이터를 불러와 base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseServiceAccountBase64);
            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase 초기화 완료");
            }
        } catch (Exception e) {
            log.error("❌ Firebase 초기화 실패: {}", e.getMessage());
        }
    }
}
