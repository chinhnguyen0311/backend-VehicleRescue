package com.Doantotnghiep.vehicle_rescue.rescue_management.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateReportRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.ProfileResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
import com.Doantotnghiep.vehicle_rescue.system.repository.ReportRepository;
import com.Doantotnghiep.vehicle_rescue.system.service.FcmTokenCacheService;
import com.Doantotnghiep.vehicle_rescue.system.service.FirebaseStorageService;
import com.Doantotnghiep.vehicle_rescue.system.service.NotificationService;
import com.Doantotnghiep.vehicle_rescue.system.service.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MechanicProfileServiceTest {
    @InjectMocks
    private MechanicProfileService service;

    @Mock
    private AccountRepository accountRepository;
    @Mock private MechanicRepository mechanicRepository;
    @Mock private MechanicServiceRepository mechanicServiceRepository;
    @Mock private DistanceService distanceService;
    @Mock private RescueOrderRepository rescueOrderRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private MechanicSubscriptionRepository subscriptionRepository;
    @Mock private SmsService smsService;
    @Mock private ReportRepository reportRepository;
    @Mock private FirebaseStorageService storageService;
    @Mock private FcmTokenCacheService tokenCacheService;
    @Mock private NotificationService notificationService;

    private Account account;
    private Mechanic mechanic;
    @BeforeEach
    void setup() {
        account = new Account();
        account.setUsername("test");
        account.setFullName("Nguyen Duy Chinh");
        account.setPhoneNumber("0834421130");

        mechanic = new Mechanic();
        mechanic.setMechanicId(UUID.randomUUID());
        mechanic.setAccount(account);
        mechanic.setWorkType(MechanicWorkType.MOBILE);
        mechanic.setDisplayName("Nguyen Duy Chinh");

        mockStatic(SecurityUtil.class);
        when(SecurityUtil.getCurrentUserLogin()).thenReturn(Optional.of("test"));

        when(accountRepository.findByUsername("test"))
                .thenReturn(Optional.of(account));

        when(mechanicRepository.findByAccount(account))
                .thenReturn(Optional.of(mechanic));
    }
    @Test
    void getProfile_success() {
        ProfileResponseDTO result = service.getProfile();

        assertEquals("Nguyen Duy Chinh", result.getFullName());
        assertEquals("0834421130", result.getPhoneNumber());
    }
    @Test
    void addService_success() {
        AddMechanicServiceRequestDTO request = new AddMechanicServiceRequestDTO();
        request.setServiceId(UUID.randomUUID());

        when(mechanicServiceRepository.existsByMechanicIdAndServiceId(any(), any()))
                .thenReturn(false);

        service.addService(request);

        verify(mechanicServiceRepository).save(any());
    }
    @Test
    void addService_duplicate_throwException() {
        AddMechanicServiceRequestDTO request = new AddMechanicServiceRequestDTO();
        request.setServiceId(UUID.randomUUID());

        when(mechanicServiceRepository.existsByMechanicIdAndServiceId(any(), any()))
                .thenReturn(true);

        assertThrows(CustomException.class, () -> service.addService(request));
    }
    @Test
    void acceptOrder_success() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = new RescueOrder();
        order.setOrderId(orderId);
        order.setMechanicId(mechanic.getMechanicId());
        order.setStatus(OrderStatus.REQUESTED);

        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any()))
                .thenReturn(false);

        when(rescueOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        service.acceptOrder(orderId);

        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        verify(rescueOrderRepository).save(order);
    }
    @Test
    void acceptOrder_hasActiveOrder_throwException() {
        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any()))
                .thenReturn(true);

        assertThrows(CustomException.class,
                () -> service.acceptOrder(UUID.randomUUID()));
    }
    @Test
    void cancelOrder_success() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = new RescueOrder();
        order.setOrderId(orderId);
        order.setMechanicId(mechanic.getMechanicId());
        order.setStatus(OrderStatus.REQUESTED);

        when(rescueOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        service.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
    @Test
    void completeOrder_success() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = new RescueOrder();
        order.setOrderId(orderId);
        order.setMechanicId(mechanic.getMechanicId());
        order.setStatus(OrderStatus.IN_PROGRESS);

        when(rescueOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        service.completeOrder(orderId);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getCompletedAt());
    }
    @Test
    void requestRenewal_success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(storageService.uploadFile(file)).thenReturn("url");

        service.requestRenewal(file);

        verify(subscriptionRepository).save(any());
    }
    @Test
    void reportByMechanic_customer_success() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = new RescueOrder();
        order.setOrderId(orderId);
        order.setMechanicId(mechanic.getMechanicId());
        order.setCustomerPhone("0999999999");

        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(orderId);
        request.setTargetType(RelatedType.CUSTOMER);

        when(rescueOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        service.reportByMechanic(request);

        verify(reportRepository).save(any());
    }

}
