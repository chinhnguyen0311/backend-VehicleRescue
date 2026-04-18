package com.Doantotnghiep.vehicle_rescue.system.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 1. Lấy email từ application.yml để làm địa chỉ người gửi
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendNewOrderNotification(String toEmail,
                                         String customerName,
                                         String address,
                                         String serviceName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 2. BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ GỬI HTML
            helper.setFrom(fromEmail, "Vehicle Rescue System");

            // Đảm bảo toEmail không bị null
            if (toEmail == null || toEmail.isEmpty()) {
                throw new IllegalArgumentException("Email người nhận không được để trống");
            }

            helper.setTo(toEmail);
            helper.setSubject("🚨 Có yêu cầu cứu hộ mới!");

            // Xử lý trường hợp serviceName bị null để hiển thị cho đẹp
            String displayService = (serviceName != null) ? serviceName : "Đang cập nhật";

            String content = """
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2 style="color: #d32f2f;">🚨 Yêu cầu cứu hộ mới</h2>
                
                <p>Xin chào,</p>
                <p>Bạn vừa nhận được một yêu cầu cứu hộ mới từ khách hàng:</p>
                
                <table style="border-collapse: collapse; width: 100%%; margin-top: 10px;">
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">👤 Khách hàng:</td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">🛠️ Dịch vụ:</td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">📍 Địa chỉ:</td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                </table>
                
                <p style="margin-top: 15px;">
                    👉 Vui lòng đăng nhập hệ thống để nhận và xử lý đơn.
                </p>
                
                <hr/>
                <p style="font-size: 12px; color: gray;">
                    Vehicle Rescue System - Thông báo tự động
                </p>
            </div>
        """.formatted(customerName, displayService, address);

            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {

            System.err.println("=== LỖI GỬI EMAIL CHI TIẾT ===");
            e.printStackTrace();
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }
}
