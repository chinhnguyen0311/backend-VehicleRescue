package com.Doantotnghiep.vehicle_rescue.system.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    @Value("${vehicle-rescue.firebase.storage.bucket}")
    private String bucketName;

    @PostConstruct
    public void initialize() {
        try {
            // Kiểm tra xem Firebase đã được khởi tạo chưa để tránh lỗi initialize nhiều lần
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = new ClassPathResource("firebase-credentials.json").getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(bucketName)
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase đã được khởi tạo thành công lúc khởi động Server!");
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khởi tạo Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
