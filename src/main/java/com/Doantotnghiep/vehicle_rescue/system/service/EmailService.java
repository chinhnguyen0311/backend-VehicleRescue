package com.Doantotnghiep.vehicle_rescue.system.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
    public void sendAccountApprovalNotification(String toEmail, String mechanicName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Vehicle Rescue System");
            helper.setTo(toEmail);
            helper.setSubject("✅ Tài khoản của bạn đã được phê duyệt!");

            String content = """
        <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <h2 style="color: #2e7d32;">✅ Chúc mừng bạn!</h2>
            
            <p>Xin chào <strong>%s</strong>,</p>
            <p>Chúng tôi vui mừng thông báo rằng tài khoản của bạn đã được Admin phê duyệt thành công.</p>
            
            <p>Bây giờ bạn có thể đăng nhập vào hệ thống để bắt đầu nhận các yêu cầu cứu hộ từ khách hàng.</p>
            
            <div style="background-color: #f1f8e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                <p><strong>Thông tin tài khoản:</strong></p>
                <ul style="margin: 0; padding-left: 20px;">
                    <li>Trạng thái: <strong>Đang hoạt động (Active)</strong></li>
                    <li>Gói dịch vụ: <strong>Đã kích hoạt</strong> (Thời hạn 30 ngày)</li>
                </ul>
            </div>
            
            <p>👉 <a href="http://your-system-url.com/login" style="color: #2e7d32;">Đăng nhập ngay tại đây</a></p>
            
            <hr/>
            <p style="font-size: 12px; color: gray;">
                Vehicle Rescue System - Hệ thống hỗ trợ cứu hộ
            </p>
        </div>
        """.formatted(mechanicName);

            helper.setText(content, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("=== LỖI GỬI EMAIL DUYỆT TÀI KHOẢN ===");
            e.printStackTrace();
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }
    public void sendAccountRejectionNotification(String toEmail, String mechanicName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Vehicle Rescue System");
            helper.setTo(toEmail);
            helper.setSubject("Thông báo kết quả đăng ký tài khoản");

            String content = """
        <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <h2 style="color: #d32f2f;">Kết quả xét duyệt tài khoản</h2>
            
            <p>Xin chào <strong>%s</strong>,</p>
            <p>Cảm ơn bạn đã quan tâm và đăng ký trở thành đối tác cứu hộ trên hệ thống của chúng tôi.</p>
            
            <p>Sau khi xem xét hồ sơ, chúng tôi rất tiếc phải thông báo rằng tài khoản của bạn <strong>chưa được phê duyệt</strong> trong đợt này.</p>
            <p>Bạn có thể bổ sung thông tin hoặc chỉnh sửa hồ sơ và đăng ký lại vào một thời điểm khác. Nếu cần hỗ trợ thêm, vui lòng liên hệ với quản trị viên.</p>
            
            <hr/>
            <p style="font-size: 12px; color: gray;">
                Vehicle Rescue System - Hệ thống hỗ trợ cứu hộ
            </p>
        </div>
        """.formatted(mechanicName);

            helper.setText(content, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("=== LỖI GỬI EMAIL TỪ CHỐI TÀI KHOẢN ===");
            e.printStackTrace();
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }
    public void sendSubscriptionRenewalNotification(String toEmail, String mechanicName, OffsetDateTime newEndDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Format ngày tháng cho đẹp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = newEndDate.format(formatter);

            helper.setFrom(fromEmail, "Vehicle Rescue System");
            helper.setTo(toEmail);
            helper.setSubject("✅ Gia hạn gói dịch vụ thành công");

            String content = """
        <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <h2 style="color: #2e7d32;">✅ Gia hạn thành công!</h2>
            
            <p>Xin chào <strong>%s</strong>,</p>
            <p>Yêu cầu gia hạn gói dịch vụ cứu hộ của bạn đã được quản trị viên phê duyệt thành công.</p>
            
            <div style="background-color: #f1f8e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                <p><strong>Thông tin gói dịch vụ:</strong></p>
                <ul style="margin: 0; padding-left: 20px;">
                    <li>Trạng thái: <strong>Đã gia hạn</strong></li>
                    <li>Ngày hết hạn mới: <strong>%s</strong></li>
                </ul>
            </div>
            
            <p>Cảm ơn bạn đã tiếp tục đồng hành cùng hệ thống. Chúc bạn có nhiều đơn cứu hộ!</p>
            
            <hr/>
            <p style="font-size: 12px; color: gray;">
                Vehicle Rescue System - Thông báo tự động
            </p>
        </div>
        """.formatted(mechanicName, formattedDate);

            helper.setText(content, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("=== LỖI GỬI EMAIL GIA HẠN ===");
            e.printStackTrace();
            throw new RuntimeException("Gửi email gia hạn thất bại: " + e.getMessage(), e);
        }
    }
}
