package com.Doantotnghiep.vehicle_rescue.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(9999, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    // Auth
    ACCOUNT_NOT_FOUND(1001, "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1002, "Mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXISTS(1003, "Tên đăng nhập đã được sử dụng", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1004, "Email đã được đăng ký", HttpStatus.CONFLICT),
    PHONE_NUMBER_ALREADY_EXISTS(1005, "Số điện thoại đã được sử dụng", HttpStatus.CONFLICT),
    INVALID_OLD_PASSWORD(1005, "Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1006, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1007, "Refresh token không hợp lệ hoặc đã bị thu hồi", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN(1009, "Access token không hợp lệ", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1008, "Tài khoản không có quyền truy cập", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_ACTIVE(1010, "Tài khoản không hoạt động", HttpStatus.FORBIDDEN),
    PASSWORD_MISMATCH(1011, "Mật khẩu không khớp", HttpStatus.BAD_REQUEST),
    GARAGE_NAME_REQUIRED(2001, "Tên garage không được để trống", HttpStatus.BAD_REQUEST),
    GARAGE_ADDRESS_REQUIRED(2002, "Địa chỉ garage không được để trống", HttpStatus.BAD_REQUEST),
    GARAGE_LOCATION_REQUIRED(2003, "Vị trí garage không được để trống", HttpStatus.BAD_REQUEST),

    // Mechanic Service
    SERVICE_ALREADY_EXISTS(3001, "Dịch vụ đã được thêm trước đó", HttpStatus.CONFLICT),
    SERVICE_NOT_FOUND(3002, "Dịch vụ không tồn tại", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(4001, "Đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    ORDER_INVALID_STATUS(4002, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_IN_PROGRESS(4003, "Bạn đang có đơn đang xử lý, không thể nhận thêm",HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(4004, "Địa chỉ không hợp lệ hoặc không tìm thấy", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(4000, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_NOT_FOUND(5001, "Gói cước không tồn tại", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_INVALID_STATUS(5002, "Trạng thái gói cước không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_COMPLETED(4005, "Đơn hàng đã hoàn thành, không thể báo cáo, hãy đánh giá", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4006, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_REPORT_TARGET(4007, "Đối tượng báo cáo không hợp lệ", HttpStatus.BAD_REQUEST)
    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
