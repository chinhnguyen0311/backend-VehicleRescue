package com.Doantotnghiep.vehicle_rescue.rescue_management.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateReportRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.LocationMessage;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
import com.Doantotnghiep.vehicle_rescue.system.repository.ReportRepository;
import com.Doantotnghiep.vehicle_rescue.system.service.FcmTokenCacheService;
import com.Doantotnghiep.vehicle_rescue.system.service.FirebaseStorageService;
import com.Doantotnghiep.vehicle_rescue.system.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MechanicProfileServiceTest {

    @InjectMocks
    private MechanicProfileService service;

    @Mock private AccountRepository accountRepository;
    @Mock private MechanicRepository mechanicRepository;
    @Mock private MechanicServiceRepository mechanicServiceRepository;
    @Mock private DistanceService distanceService;
    @Mock private RescueOrderRepository rescueOrderRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private MechanicSubscriptionRepository subscriptionRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private FirebaseStorageService storageService;
    @Mock private FcmTokenCacheService tokenCacheService;
    @Mock private NotificationService notificationService;

    private Account account;
    private Mechanic mechanic;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

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
        mechanic.setType(MechanicType.GENERAL); // tránh NPE tại mechanic.getType().name()
        mechanic.setDisplayName("Nguyen Duy Chinh");

        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserLogin)
                .thenReturn(Optional.of("test"));

        when(accountRepository.findByUsername("test")).thenReturn(Optional.of(account));
        when(mechanicRepository.findByAccount(account)).thenReturn(Optional.of(mechanic));
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtil != null) {
            mockedSecurityUtil.close();
        }
    }

    // ==================== getProfile ====================

    @Test
    void getProfile_mobile_success() {
        ProfileResponseDTO result = service.getProfile();
        assertEquals("Nguyen Duy Chinh", result.getFullName());
        assertEquals("0834421130", result.getPhoneNumber());
    }

    @Test
    void getProfile_garage_includesGarageFields() {
        mechanic.setWorkType(MechanicWorkType.GARAGE);
        mechanic.setGarageName("Garage ABC");
        mechanic.setGarageAddress("123 Pho Hue");

        ProfileResponseDTO result = service.getProfile();

        assertEquals("Garage ABC", result.getGarageName());
        assertEquals("123 Pho Hue", result.getGarageAddress());
    }

    // ==================== addService ====================

    @Test
    void addService_success() {
        AddMechanicServiceRequestDTO request = new AddMechanicServiceRequestDTO();
        request.setServiceId(UUID.randomUUID());

        when(mechanicServiceRepository.existsByMechanicIdAndServiceId(any(), any())).thenReturn(false);

        service.addService(request);

        verify(mechanicServiceRepository).save(any());
    }

    @Test
    void addService_duplicate_throwException() {
        AddMechanicServiceRequestDTO request = new AddMechanicServiceRequestDTO();
        request.setServiceId(UUID.randomUUID());

        when(mechanicServiceRepository.existsByMechanicIdAndServiceId(any(), any())).thenReturn(true);

        assertThrows(CustomException.class, () -> service.addService(request));
    }

    // ==================== getServices ====================

    @Test
    void getServices_success() {
        when(mechanicServiceRepository.findServicesByMechanicId(mechanic.getMechanicId()))
                .thenReturn(List.of());

        List<MechanicServiceResponseDTO> result = service.getServices();

        assertNotNull(result);
        verify(mechanicServiceRepository).findServicesByMechanicId(mechanic.getMechanicId());
    }

    // ==================== updateServicePrice ====================

    @Test
    void updateServicePrice_success() {
        UUID serviceId = UUID.randomUUID();
        UpdateMechanicServiceRequestDTO request = new UpdateMechanicServiceRequestDTO();
        request.setCustomPrice(BigDecimal.valueOf(150000));

        MechanicService ms = new MechanicService();
        when(mechanicServiceRepository.findById(any())).thenReturn(Optional.of(ms));

        service.updateServicePrice(serviceId, request);

        verify(mechanicServiceRepository).save(ms);
        assertEquals(BigDecimal.valueOf(150000), ms.getCustomPrice());
    }

    @Test
    void updateServicePrice_notFound_throwException() {
        when(mechanicServiceRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class,
                () -> service.updateServicePrice(UUID.randomUUID(), new UpdateMechanicServiceRequestDTO()));
    }

    // ==================== getRequestedOrdersForMechanic ====================

    @Test
    void getRequestedOrders_noOrders_returnsEmpty() {
        when(rescueOrderRepository.findTodayRequestedOrders(any(), any(), any()))
                .thenReturn(List.of());

        List<MechanicOrderItemResponse> result = service.getRequestedOrdersForMechanic();

        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestedOrders_withServiceId_returnsMapped() {
        UUID serviceId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.REQUESTED);
        order.setServiceId(serviceId);
        order.setCustomerName("Customer A");

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Cứu hộ xe máy");

        when(rescueOrderRepository.findTodayRequestedOrders(any(), any(), any())).thenReturn(List.of(order));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));
        when(distanceService.getDistance(any(), any(), any(), any())).thenReturn(1.5);
        when(distanceService.getAddress(any(), any())).thenReturn("Hà Nội");

        List<MechanicOrderItemResponse> result = service.getRequestedOrdersForMechanic();

        assertEquals(1, result.size());
        assertEquals("Cứu hộ xe máy", result.get(0).getServiceName());
    }

    @Test
    void getRequestedOrders_noServiceId_returnsDefaultName() {
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.REQUESTED);
        order.setServiceId(null);

        when(rescueOrderRepository.findTodayRequestedOrders(any(), any(), any())).thenReturn(List.of(order));
        when(distanceService.getDistance(any(), any(), any(), any())).thenReturn(0.0);
        when(distanceService.getAddress(any(), any())).thenReturn("");

        List<MechanicOrderItemResponse> result = service.getRequestedOrdersForMechanic();

        assertEquals("Không xác định", result.get(0).getServiceName());
    }

    // ==================== acceptOrder ====================

    @Test
    void acceptOrder_success_noFcmToken() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.REQUESTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any())).thenReturn(false);
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn(null);

        service.acceptOrder(orderId);

        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        verify(rescueOrderRepository).save(order);
        verify(notificationService, never()).sendPushNotification(any(), any(), any());
    }

    @Test
    void acceptOrder_success_withFcmToken_sendsNotification() throws InterruptedException {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.REQUESTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any())).thenReturn(false);
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn("fcm-token");

        service.acceptOrder(orderId);

        Thread.sleep(300);
        verify(notificationService).sendPushNotification(eq("fcm-token"), any(), any());
    }

    @Test
    void acceptOrder_hasActiveOrder_throwException() {
        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any())).thenReturn(true);

        assertThrows(CustomException.class, () -> service.acceptOrder(UUID.randomUUID()));
    }

    @Test
    void acceptOrder_orderNotBelongToMechanic_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(UUID.randomUUID(), OrderStatus.REQUESTED); // mechanic khác
        order.setOrderId(orderId);

        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any())).thenReturn(false);
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.acceptOrder(orderId));
    }

    @Test
    void acceptOrder_invalidStatus_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.CANCELLED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.existsByMechanicIdAndStatusIn(any(), any())).thenReturn(false);
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.acceptOrder(orderId));
    }

    // ==================== cancelOrder ====================

    @Test
    void cancelOrder_success_noFcmToken() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.REQUESTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn(null);

        service.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancelOrder_withFcmToken_sendsNotification() throws InterruptedException {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.ACCEPTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn("fcm-token");

        service.cancelOrder(orderId);

        Thread.sleep(300);
        verify(notificationService).sendPushNotification(eq("fcm-token"), any(), any());
    }

    @Test
    void cancelOrder_orderNotBelongToMechanic_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(UUID.randomUUID(), OrderStatus.REQUESTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.cancelOrder(orderId));
    }

    @Test
    void cancelOrder_invalidStatus_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.COMPLETED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.cancelOrder(orderId));
    }

    // ==================== inProcessOrder ====================

    @Test
    void inProcessOrder_success_noFcmToken() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.ACCEPTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn(null);

        service.inProcessOrder(orderId);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        verify(rescueOrderRepository).save(order);
    }

    @Test
    void inProcessOrder_withFcmToken_sendsNotification() throws InterruptedException {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.ACCEPTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn("fcm-token");

        service.inProcessOrder(orderId);

        Thread.sleep(300);
        verify(notificationService).sendPushNotification(eq("fcm-token"), any(), any());
    }

    @Test
    void inProcessOrder_orderNotBelongToMechanic_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(UUID.randomUUID(), OrderStatus.ACCEPTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.inProcessOrder(orderId));
    }

    @Test
    void inProcessOrder_invalidStatus_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.REQUESTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.inProcessOrder(orderId));
    }

    // ==================== completeOrder ====================

    @Test
    void completeOrder_success_noFcmToken() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.IN_PROGRESS);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn(null);

        service.completeOrder(orderId);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getCompletedAt());
    }

    @Test
    void completeOrder_withFcmToken_sendsNotification() throws InterruptedException {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.IN_PROGRESS);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(tokenCacheService.getTokenByOrderId(orderId)).thenReturn("fcm-token");

        service.completeOrder(orderId);

        Thread.sleep(300);
        verify(notificationService).sendPushNotification(eq("fcm-token"), any(), any());
    }

    @Test
    void completeOrder_orderNotBelongToMechanic_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(UUID.randomUUID(), OrderStatus.IN_PROGRESS);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.completeOrder(orderId));
    }

    @Test
    void completeOrder_invalidStatus_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.ACCEPTED);
        order.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.completeOrder(orderId));
    }

    // ==================== getCurrentOrder ====================

    @Test
    void getCurrentOrder_noActiveOrder_returnsNull() {
        when(rescueOrderRepository.findFirstByMechanicIdAndStatusIn(any(), any()))
                .thenReturn(Optional.empty());

        assertNull(service.getCurrentOrder());
    }

    @Test
    void getCurrentOrder_withServiceId_returnsResponse() {
        UUID serviceId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.ACCEPTED);
        order.setCustomerName("Khach Hang");
        order.setServiceId(serviceId);

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Cứu hộ ô tô");

        when(rescueOrderRepository.findFirstByMechanicIdAndStatusIn(any(), any()))
                .thenReturn(Optional.of(order));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));
        when(distanceService.getDistance(any(), any(), any(), any())).thenReturn(2.0);
        when(distanceService.getAddress(any(), any())).thenReturn("Hà Nội");

        MechanicOrderItemResponse result = service.getCurrentOrder();

        assertNotNull(result);
        assertEquals("Cứu hộ ô tô", result.getServiceName());
    }

    @Test
    void getCurrentOrder_noServiceId_returnsDefaultName() {
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.ACCEPTED);
        order.setServiceId(null);

        when(rescueOrderRepository.findFirstByMechanicIdAndStatusIn(any(), any()))
                .thenReturn(Optional.of(order));
        when(distanceService.getDistance(any(), any(), any(), any())).thenReturn(0.0);
        when(distanceService.getAddress(any(), any())).thenReturn("");

        MechanicOrderItemResponse result = service.getCurrentOrder();

        assertEquals("Không xác định", result.getServiceName());
    }

    // ==================== getOrderHistory ====================

    @Test
    void getOrderHistory_empty_returnsEmptyList() {
        when(rescueOrderRepository.findCompletedOrdersByMechanic(any())).thenReturn(List.of());

        assertTrue(service.getOrderHistory().isEmpty());
    }

    @Test
    void getOrderHistory_withReview_returnsMapped() {
        UUID orderId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.COMPLETED);
        order.setOrderId(orderId);
        order.setCustomerName("Khach A");
        order.setServiceId(serviceId);
        order.setCompletedAt(OffsetDateTime.now());

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Vá xe");

        Review review = new Review();
        review.setRating(5);
        review.setReview("Tốt lắm");

        when(rescueOrderRepository.findCompletedOrdersByMechanic(any())).thenReturn(List.of(order));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));
        when(reviewRepository.findByOrderId(orderId)).thenReturn(Optional.of(review));

        List<MechanicOrderHistoryResponse> result = service.getOrderHistory();

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
        assertEquals("Tốt lắm", result.get(0).getReview());
    }

    @Test
    void getOrderHistory_noReview_returnsNullRating() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.COMPLETED);
        order.setOrderId(orderId);
        order.setServiceId(null);

        when(rescueOrderRepository.findCompletedOrdersByMechanic(any())).thenReturn(List.of(order));
        when(reviewRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        List<MechanicOrderHistoryResponse> result = service.getOrderHistory();

        assertNull(result.get(0).getRating());
    }

    // ==================== updateLocationRealtime ====================

    @Test
    void updateLocation_mobile_updatesLocation() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test");
        mechanic.setWorkType(MechanicWorkType.MOBILE);

        LocationMessage request = new LocationMessage();
        request.setLatitude(21.0);
        request.setLongitude(105.0);

        service.updateLocationRealtime(request, principal);

        verify(mechanicRepository).save(mechanic);
    }

    @Test
    void updateLocation_garage_skipsUpdate() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test");
        mechanic.setWorkType(MechanicWorkType.GARAGE);

        service.updateLocationRealtime(new LocationMessage(), principal);

        verify(mechanicRepository, never()).save(any());
    }

    @Test
    void updateLocation_nullPrincipal_throwException() {
        assertThrows(CustomException.class,
                () -> service.updateLocationRealtime(new LocationMessage(), null));
    }

    // ==================== getStatistic ====================

    @Test
    void getStatistic_noFirstOrder_avgPerMonthIsZero() {
        when(rescueOrderRepository.countCompleted(any())).thenReturn(5);
        when(reviewRepository.getAverageRating(any())).thenReturn(4.0);
        when(reviewRepository.countReviews(any())).thenReturn(10L);
        when(rescueOrderRepository.getFirstOrderDate(any())).thenReturn(null);
        when(reviewRepository.getMechanicStats()).thenReturn(List.of());
        when(reviewRepository.findTop3Recent(any())).thenReturn(List.of());

        MechanicStatisticResponse result = service.getStatistic();

        assertEquals(5, result.getTotalCompletedOrders());
        assertEquals(0.0, result.getAvgOrdersPerMonth());
    }

    @Test
    void getStatistic_nullRatingAndReviews_defaultsToZero() {
        when(rescueOrderRepository.countCompleted(any())).thenReturn(0);
        when(reviewRepository.getAverageRating(any())).thenReturn(null);
        when(reviewRepository.countReviews(any())).thenReturn(null);
        when(rescueOrderRepository.getFirstOrderDate(any())).thenReturn(null);
        when(reviewRepository.getMechanicStats()).thenReturn(List.of());
        when(reviewRepository.findTop3Recent(any())).thenReturn(List.of());

        MechanicStatisticResponse result = service.getStatistic();

        assertEquals(BigDecimal.valueOf(0.0), result.getAverageRating());
    }

    @Test
    void getStatistic_withFirstOrderDate_calculatesAvgPerMonth() {
        when(rescueOrderRepository.countCompleted(any())).thenReturn(6);
        when(reviewRepository.getAverageRating(any())).thenReturn(4.0);
        when(reviewRepository.countReviews(any())).thenReturn(6L);
        when(rescueOrderRepository.getFirstOrderDate(any()))
                .thenReturn(OffsetDateTime.now().minusDays(60));
        when(reviewRepository.getMechanicStats()).thenReturn(List.of());
        when(reviewRepository.findTop3Recent(any())).thenReturn(List.of());

        MechanicStatisticResponse result = service.getStatistic();

        assertTrue(result.getAvgOrdersPerMonth() > 0);
    }

    @Test
    void getStatistic_withReviews_mapsItems() {
        UUID serviceId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.COMPLETED);
        order.setCustomerName("Khach B");
        order.setServiceId(serviceId);

        Review review = new Review();
        review.setRating(4);
        review.setReview("OK");
        review.setOrder(order);
        review.setCreatedAt(OffsetDateTime.now());

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Vá xe");

        when(rescueOrderRepository.countCompleted(any())).thenReturn(1);
        when(reviewRepository.getAverageRating(any())).thenReturn(4.0);
        when(reviewRepository.countReviews(any())).thenReturn(1L);
        when(rescueOrderRepository.getFirstOrderDate(any())).thenReturn(null);
        when(reviewRepository.getMechanicStats()).thenReturn(List.of());
        when(reviewRepository.findTop3Recent(any())).thenReturn(List.of(review));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));

        MechanicStatisticResponse result = service.getStatistic();

        assertEquals(1, result.getRecentReviews().size());
        assertEquals("Vá xe", result.getRecentReviews().get(0).getServiceName());
    }

    // ==================== getMechanicRank ====================

    @Test
    void getMechanicRank_notInList_returnsMinusOne() {
        List<Object[]> stats = new ArrayList<>();
        stats.add(new Object[]{UUID.randomUUID(), 10, 4.5});

        when(reviewRepository.getMechanicStats()).thenReturn(stats);

        assertEquals(-1, service.getMechanicRank(UUID.randomUUID()));
    }

    @Test
    void getMechanicRank_emptyStats_returnsMinusOne() {
        when(reviewRepository.getMechanicStats())
                .thenReturn(Collections.emptyList());

        assertEquals(-1, service.getMechanicRank(UUID.randomUUID()));
    }

// ==================== getMechanicDetail ====================

    @Test
    void getMechanicDetail_mobile_success() {
        UUID id = mechanic.getMechanicId();

        when(mechanicRepository.findById(id)).thenReturn(Optional.of(mechanic));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));

        List<Object[]> reviewStats = new ArrayList<>();
        reviewStats.add(new Object[]{4.2, 15L});

        when(reviewRepository.getReviewStats(id)).thenReturn(reviewStats);

        MechanicDetailResponse result = service.getMechanicDetail(id);

        assertEquals("Nguyen Duy Chinh", result.getMechanicName());
        assertEquals(4.2, result.getAvgRating());
        assertNull(result.getAddress());
    }

    @Test
    void getMechanicDetail_garage_includesAddress() {
        mechanic.setWorkType(MechanicWorkType.GARAGE);
        mechanic.setGarageName("Garage XYZ");
        mechanic.setGarageAddress("456 Nguyen Trai");

        UUID id = mechanic.getMechanicId();
        when(mechanicRepository.findById(id)).thenReturn(Optional.of(mechanic));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(reviewRepository.getReviewStats(id)).thenReturn(List.of());

        MechanicDetailResponse result = service.getMechanicDetail(id);

        assertEquals("Garage XYZ", result.getMechanicName());
        assertEquals("456 Nguyen Trai", result.getAddress());
    }

    @Test
    void getMechanicDetail_emptyReviewStats_defaultsToZero() {
        UUID id = mechanic.getMechanicId();
        when(mechanicRepository.findById(id)).thenReturn(Optional.of(mechanic));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(reviewRepository.getReviewStats(id)).thenReturn(List.of());

        MechanicDetailResponse result = service.getMechanicDetail(id);

        assertEquals(0.0, result.getAvgRating());
        assertEquals(0L, result.getTotalReviews());
    }

    @Test
    void getMechanicDetail_notFound_throwException() {
        when(mechanicRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class,
                () -> service.getMechanicDetail(UUID.randomUUID()));
    }

    // ==================== requestRenewal ====================

    @Test
    void requestRenewal_noExistingSubsEndDate() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        mechanic.setSubsEndDate(null);
        when(storageService.uploadFile(file)).thenReturn("http://bill.url");

        service.requestRenewal(file);

        verify(subscriptionRepository).save(any());
    }

    @Test
    void requestRenewal_expiredSubsEndDate_resetsFrom_now() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        mechanic.setSubsEndDate(OffsetDateTime.now().minusDays(5));
        when(storageService.uploadFile(file)).thenReturn("http://bill.url");

        service.requestRenewal(file);

        verify(subscriptionRepository).save(any());
    }

    @Test
    void requestRenewal_activeSubsEndDate_extendsFromCurrentEnd() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        mechanic.setSubsEndDate(OffsetDateTime.now().plusDays(10));
        when(storageService.uploadFile(file)).thenReturn("http://bill.url");

        service.requestRenewal(file);

        verify(subscriptionRepository).save(any());
    }

    // ==================== reportByMechanic ====================

    @Test
    void reportByMechanic_customer_success() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(mechanic.getMechanicId(), OrderStatus.COMPLETED);
        order.setOrderId(orderId);
        order.setCustomerPhone("0999999999");

        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(orderId);
        request.setTargetType(RelatedType.CUSTOMER);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.reportByMechanic(request);

        verify(reportRepository).save(any());
    }

    @Test
    void reportByMechanic_system_noOrder_success() {
        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(null);
        request.setTargetType(RelatedType.SYSTEM);

        service.reportByMechanic(request);

        verify(reportRepository).save(any());
    }

    @Test
    void reportByMechanic_customer_noOrder_throwException() {
        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(null);
        request.setTargetType(RelatedType.CUSTOMER);

        assertThrows(CustomException.class, () -> service.reportByMechanic(request));
    }

    @Test
    void reportByMechanic_orderNotBelongToMechanic_throwException() {
        UUID orderId = UUID.randomUUID();
        RescueOrder order = buildOrder(UUID.randomUUID(), OrderStatus.COMPLETED);
        order.setOrderId(orderId);

        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(orderId);
        request.setTargetType(RelatedType.CUSTOMER);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.reportByMechanic(request));
    }

    // ==================== helper ====================

    private RescueOrder buildOrder(UUID mechanicId, OrderStatus status) {
        RescueOrder order = new RescueOrder();
        order.setOrderId(UUID.randomUUID());
        order.setMechanicId(mechanicId);
        order.setStatus(status);
        order.setCreatedAt(OffsetDateTime.now());
        return order;
    }
}