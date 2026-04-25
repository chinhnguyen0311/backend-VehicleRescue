//package com.Doantotnghiep.vehicle_rescue.system.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Configuration
//public class FirebaseConfig {
//    @PostConstruct
//    public void initializeFirebase() {
//        try {
//            // Đọc file từ src/main/resources
//            InputStream serviceAccount = new ClassPathResource("firebase-credentials.json").getInputStream();
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            // Chỉ khởi tạo nếu chưa có app nào được khởi tạo (tránh lỗi duplicate)
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Không tìm thấy hoặc không thể đọc file firebase-credentials.json. Hãy kiểm tra lại thư mục resources!", e);
//        }
//    }
//}
