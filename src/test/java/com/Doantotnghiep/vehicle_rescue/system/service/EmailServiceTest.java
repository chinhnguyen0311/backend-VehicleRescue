package com.Doantotnghiep.vehicle_rescue.system.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Set giá trị cho thuộc tính @Value("${spring.mail.username}")
        ReflectionTestUtils.setField(emailService, "fromEmail", "admin@vehiclerescue.com");

        // Luôn trả về mimeMessage mock khi createMimeMessage được gọi
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // =======================================================================
    // Test sendNewOrderNotification
    // =======================================================================

    @Test
    void sendNewOrderNotification_success() {
        String toEmail = "mechanic@example.com";
        String customerName = "Nguyen Van A";
        String address = "123 Xuan Thuy, Ha Noi";
        String serviceName = "Vá lốp xe máy";

        emailService.sendNewOrderNotification(toEmail, customerName, address, serviceName);

        // Verify rằng hàm send đã được gọi với MimeMessage đúng 1 lần
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendNewOrderNotification_nullServiceName_success() {
        // Test trường hợp serviceName bị null như trong logic code
        emailService.sendNewOrderNotification("mechanic@example.com", "Nguyen Van A", "123 Xuan Thuy", null);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendNewOrderNotification_nullEmail_throwsException() {
        // Khi email null, hàm sẽ ném ra IllegalArgumentException, sau đó bị catch và ném lại dưới dạng RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                emailService.sendNewOrderNotification(null, "Nguyen Van A", "123 Xuan Thuy", "Vá lốp")
        );

        assertTrue(exception.getMessage().contains("Gửi email thất bại"));
        verify(mailSender, never()).send(any(MimeMessage.class)); // Đảm bảo không gửi mail
    }

    @Test
    void sendNewOrderNotification_mailSenderThrowsException_throwsRuntimeException() {
        // Giả lập lỗi từ mailSender
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                emailService.sendNewOrderNotification("mechanic@example.com", "Nguyen", "HN", "Vá lốp")
        );

        assertTrue(exception.getMessage().contains("Mail server down"));
    }

    // =======================================================================
    // Test sendAccountApprovalNotification
    // =======================================================================

    @Test
    void sendAccountApprovalNotification_success() {
        emailService.sendAccountApprovalNotification("mechanic@example.com", "Tran Van B");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendAccountApprovalNotification_throwsException() {
        doThrow(new RuntimeException("Connection timeout")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                emailService.sendAccountApprovalNotification("mechanic@example.com", "Tran Van B")
        );

        assertTrue(exception.getMessage().contains("Gửi email thất bại"));
    }

    // =======================================================================
    // Test sendAccountRejectionNotification
    // =======================================================================

    @Test
    void sendAccountRejectionNotification_success() {
        emailService.sendAccountRejectionNotification("mechanic@example.com", "Le Van C");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendAccountRejectionNotification_throwsException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                emailService.sendAccountRejectionNotification("mechanic@example.com", "Le Van C")
        );

        assertTrue(exception.getMessage().contains("SMTP error"));
    }

    // =======================================================================
    // Test sendSubscriptionRenewalNotification
    // =======================================================================

    @Test
    void sendSubscriptionRenewalNotification_success() {
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(30);
        emailService.sendSubscriptionRenewalNotification("mechanic@example.com", "Hoang Van D", endDate);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendSubscriptionRenewalNotification_throwsException() {
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(30);
        doThrow(new RuntimeException("Failed to send")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                emailService.sendSubscriptionRenewalNotification("mechanic@example.com", "Hoang Van D", endDate)
        );

        assertTrue(exception.getMessage().contains("Gửi email gia hạn thất bại"));
    }
}