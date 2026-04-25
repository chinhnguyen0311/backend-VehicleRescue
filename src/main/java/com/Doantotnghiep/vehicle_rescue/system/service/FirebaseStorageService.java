package com.Doantotnghiep.vehicle_rescue.system.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {

    @Value("${vehicle-rescue.firebase.storage.bucket}")
    private String bucketName;

    private synchronized void ensureFirebaseInitialized() throws IOException {
        try {
            FirebaseApp.getInstance();
            return;
        } catch (IllegalStateException e) {
            InputStream serviceAccount = new ClassPathResource("firebase-credentials.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(bucketName) // <-- Thêm dòng này
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        ensureFirebaseInitialized();

        FirebaseApp app = FirebaseApp.getInstance();
        // Sử dụng bucketName đã cấu hình
        Bucket bucket = StorageClient.getInstance(app).bucket(bucketName);

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Thêm log kiểm tra xem bucket có null không
        if (bucket == null) {
            throw new RuntimeException("Không tìm thấy bucket: " + bucketName);
        }

        Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());
        return String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName,
                URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );
    }
}