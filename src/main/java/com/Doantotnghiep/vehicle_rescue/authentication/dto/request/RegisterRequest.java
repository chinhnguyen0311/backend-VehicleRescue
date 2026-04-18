package com.Doantotnghiep.vehicle_rescue.authentication.dto.request;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải có độ dài từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có độ dài tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Fullname không được để trống")
    private String fullName;

    private String phoneNumber;
    @NotNull(message = "Loại thợ không được để trống")
    private MechanicType type;
    @NotNull(message = "Loại hình hoạt động không được để trống")
    private MechanicWorkType workType;
    private String garageName;
    private String garageAddress;
}

