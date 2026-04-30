package com.Doantotnghiep.vehicle_rescue.rescue_management.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateReportRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateRescueOrderRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.SearchMechanicRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.mapper.RescueOrderMapper;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicServiceRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.ServiceRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import com.Doantotnghiep.vehicle_rescue.system.repository.ReportRepository;
import com.Doantotnghiep.vehicle_rescue.system.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RescueOrderServiceTest {

    @InjectMocks
    private RescueOrderService service;

    @Mock private MechanicServiceRepository mechanicServiceRepository;
    @Mock private RescueOrderRepository rescueOrderRepository;
    @Mock private MechanicRepository mechanicRepository;
    @Mock private DistanceService distanceService;
    @Mock private RescueOrderMapper rescueOrderMapper;
    @Mock private ServiceRepository serviceRepository;
    @Mock private EmailService emailService;
    @Mock private ReportRepository reportRepository;

    // ─── shared fixtures ───────────────────────────────────────────────────────

    private Mechanic buildMechanic(MechanicWorkType workType, String phone) {
        Account account = new Account();
        account.setPhoneNumber(phone);
        account.setEmail("mechanic@example.com");

        Mechanic m = new Mechanic();
        m.setMechanicId(UUID.randomUUID());
        m.setWorkType(workType);
        m.setType(MechanicType.MOTORBIKE);
        m.setDisplayName("Nguyen Van A");
        m.setGarageName("Garage ABC");
        m.setPhoneNumber(phone);
        m.setAccount(account);
        return m;
    }

    private RescueOrder buildOrder(OrderStatus status) {
        RescueOrder o = new RescueOrder();
        o.setOrderId(UUID.randomUUID());
        o.setStatus(status);
        o.setCustomerPhone("0900000001");
        o.setCustomerName("Khach Hang");
        o.setCreatedAt(OffsetDateTime.now());
        return o;
    }

    // ─── Object[] row helper: index mapping in searchNearbyMechanics ──────────
    // row[0]=mechanicId, [1]=type, [2]=displayName, [3]=phone,
    // [4]=currentLocation, [5]=garageName, [6]=garageLocation,
    // [7]=workType, [8]=ratingScore, [9]=avatarUrl, [last]=distance
    private Object[] buildRow(UUID mechanicId, String workType, Double distance) {
        return new Object[]{
                mechanicId.toString(),   // [0] mechanicId
                "MOTORBIKE",              // [1] type
                "Nguyen Van A",         // [2] displayName (MOBILE)
                "0911111111",           // [3] phone
                null,                   // [4] currentLocation (MOBILE)
                "Garage ABC",           // [5] garageName (GARAGE)
                null,                   // [6] garageLocation (GARAGE)
                workType,               // [7] workType
                "4.5",                  // [8] ratingScore
                "http://avatar.url",    // [9] avatarUrl
                distance                // [10] distance
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllServices
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getAllServices_empty_returnsEmptyList() {
        when(serviceRepository.findAll()).thenReturn(List.of());

        List<ServiceResponseDTO> result = service.getAllServices();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllServices_withServices_returnsMappedList() {
        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setServiceId(UUID.randomUUID());
        svc.setName("Cứu hộ xe máy");

        when(serviceRepository.findAll()).thenReturn(List.of(svc));

        List<ServiceResponseDTO> result = service.getAllServices();

        assertEquals(1, result.size());
        assertEquals("Cứu hộ xe máy", result.get(0).getName());
        assertNotNull(result.get(0).getServiceId());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // searchNearbyMechanics
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void searchNearbyMechanics_noResults_returnsEmpty() {
        SearchMechanicRequestDTO request = buildSearchRequest();
        when(mechanicRepository.findNearbyMechanics(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<MechanicSearchResultDTO> result = service.searchNearbyMechanics(request);

        assertTrue(result.isEmpty());
        // topList rỗng → không gọi ORS
        verify(distanceService, never()).getRealDistances(any(), any(), any());
    }

    @Test
    void searchNearbyMechanics_mobile_withResults_callsOrsAndSorts() {
        UUID mechanicId = UUID.randomUUID();
        SearchMechanicRequestDTO request = buildSearchRequest();

        Object[] row = buildRow(mechanicId, "MOBILE", 1.2);

        when(mechanicRepository.findNearbyMechanics(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row)); // ✅ FIX

        when(mechanicServiceRepository.findServicesByMechanicId(mechanicId))
                .thenReturn(Collections.emptyList());

        when(rescueOrderRepository.countByMechanicIdAndStatus(mechanicId, OrderStatus.COMPLETED))
                .thenReturn(5);

        when(distanceService.getRealDistances(any(), any(), any()))
                .thenReturn(List.of(2.5));

        List<MechanicSearchResultDTO> result = service.searchNearbyMechanics(request);

        assertEquals(1, result.size());
        assertEquals(mechanicId, result.get(0).getMechanicId());
        assertEquals("Nguyen Van A", result.get(0).getDisplayName());
        assertEquals(2.5, result.get(0).getDistance());

        verify(distanceService).getRealDistances(any(), any(), any());
    }

    @Test
    void searchNearbyMechanics_garage_usesGarageName() {
        UUID mechanicId = UUID.randomUUID();
        SearchMechanicRequestDTO request = buildSearchRequest();

        Object[] row = buildRow(mechanicId, "GARAGE", 0.8);

        when(mechanicRepository.findNearbyMechanics(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row)); // ✅ FIX

        when(mechanicServiceRepository.findServicesByMechanicId(mechanicId))
                .thenReturn(Collections.emptyList());

        when(rescueOrderRepository.countByMechanicIdAndStatus(mechanicId, OrderStatus.COMPLETED))
                .thenReturn(0);

        when(distanceService.getRealDistances(any(), any(), any()))
                .thenReturn(List.of(0.8));

        List<MechanicSearchResultDTO> result = service.searchNearbyMechanics(request);

        assertEquals("Garage ABC", result.get(0).getDisplayName());
    }

    @Test
    void searchNearbyMechanics_nullRatingAndAvatar_defaultsToZeroAndNull() {
        UUID mechanicId = UUID.randomUUID();
        SearchMechanicRequestDTO request = buildSearchRequest();

        Object[] row = new Object[]{
                mechanicId.toString(),
                "GENERAL",
                "Ten A",
                "0911",
                null,        // ratingScore
                "Garage",
                null,        // avatarUrl
                "MOBILE",
                null,
                null,
                1.0
        };

        when(mechanicRepository.findNearbyMechanics(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row)); // ✅ FIX

        when(mechanicServiceRepository.findServicesByMechanicId(mechanicId))
                .thenReturn(Collections.emptyList());

        when(rescueOrderRepository.countByMechanicIdAndStatus(any(), any()))
                .thenReturn(0);

        when(distanceService.getRealDistances(any(), any(), any()))
                .thenReturn(List.of(1.0));

        List<MechanicSearchResultDTO> result = service.searchNearbyMechanics(request);

        assertEquals(BigDecimal.ZERO, result.get(0).getRatingScore());
        assertNull(result.get(0).getAvatarUrl());
    }

    @Test
    void searchNearbyMechanics_moreThan5Results_limitsToTop5AndCallsOrs() {
        SearchMechanicRequestDTO request = buildSearchRequest();

        // Tạo 7 mechanics
        List<Object[]> rows = new ArrayList<>();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            rows.add(buildRow(id, "MOBILE", (double) i));
        }

        when(mechanicRepository.findNearbyMechanics(any(), any(), any(), any(), any()))
                .thenReturn(rows);
        for (UUID id : ids) {
            when(mechanicServiceRepository.findServicesByMechanicId(id)).thenReturn(List.of());
            when(rescueOrderRepository.countByMechanicIdAndStatus(id, OrderStatus.COMPLETED)).thenReturn(0);
        }
        when(distanceService.getRealDistances(any(), any(), any()))
                .thenReturn(List.of(1.0, 2.0, 3.0, 4.0, 5.0));

        List<MechanicSearchResultDTO> result = service.searchNearbyMechanics(request);

        // chỉ lấy tối đa 5
        assertEquals(5, result.size());
        verify(distanceService).getRealDistances(any(), any(), any());
    }

    @Test
    void searchNearbyMechanics_resultsSortedByDistance() {
        SearchMechanicRequestDTO request = buildSearchRequest();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(mechanicRepository.findNearbyMechanics(any(), any(), any(), any(), any()))
                .thenReturn(List.of(buildRow(id1, "MOBILE", 5.0), buildRow(id2, "MOBILE", 1.0)));
        when(mechanicServiceRepository.findServicesByMechanicId(any())).thenReturn(List.of());
        when(rescueOrderRepository.countByMechanicIdAndStatus(any(), any())).thenReturn(0);
        // ORS trả về khoảng cách thật: id1=5.0, id2=1.0
        when(distanceService.getRealDistances(any(), any(), any()))
                .thenReturn(List.of(5.0, 1.0));

        List<MechanicSearchResultDTO> result = service.searchNearbyMechanics(request);

        // sau sort: id2 (1.0) trước, id1 (5.0) sau
        assertEquals(id2, result.get(0).getMechanicId());
        assertEquals(id1, result.get(1).getMechanicId());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // createRescueOrder
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void createRescueOrder_mobile_withService_success() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.MOBILE, "0911111111");
        UUID serviceId = UUID.randomUUID();

        CreateRescueOrderRequest request = buildOrderRequest(
                mechanic.getMechanicId(), "0900000001", serviceId);

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Vá xe");

        RescueOrder saved = buildOrder(OrderStatus.REQUESTED);
        RescueOrderResponse response = new RescueOrderResponse();

        when(mechanicRepository.findById(mechanic.getMechanicId())).thenReturn(Optional.of(mechanic));
        when(distanceService.getAddress(any(), any())).thenReturn("Hà Nội");
        when(rescueOrderRepository.save(any())).thenReturn(saved);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));
        when(rescueOrderMapper.toResponse(saved)).thenReturn(response);

        RescueOrderResponse result = service.createRescueOrder(request);

        assertNotNull(result);
        verify(rescueOrderRepository).save(any());
        verify(emailService).sendNewOrderNotification(any(), any(), any(), eq("Vá xe"));
    }

    @Test
    void createRescueOrder_garage_usesGarageName() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.GARAGE, "0911111111");
        mechanic.setGarageName("Garage XYZ");

        CreateRescueOrderRequest request = buildOrderRequest(
                mechanic.getMechanicId(), "0900000001", null);

        RescueOrder saved = buildOrder(OrderStatus.REQUESTED);
        when(mechanicRepository.findById(mechanic.getMechanicId())).thenReturn(Optional.of(mechanic));
        when(distanceService.getAddress(any(), any())).thenReturn("TP HCM");
        when(rescueOrderRepository.save(any())).thenReturn(saved);
        when(rescueOrderMapper.toResponse(saved)).thenReturn(new RescueOrderResponse());

        service.createRescueOrder(request);

        // verify order được save với mechanicName là garageName
        verify(rescueOrderRepository).save(argThat(o ->
                ((RescueOrder) o).getMechanicName().equals("Garage XYZ")));
    }

    @Test
    void createRescueOrder_noServiceId_skipsServiceLookup() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.MOBILE, "0911111111");

        CreateRescueOrderRequest request = buildOrderRequest(
                mechanic.getMechanicId(), "0900000001", null); // serviceId = null

        RescueOrder saved = buildOrder(OrderStatus.REQUESTED);
        when(mechanicRepository.findById(mechanic.getMechanicId())).thenReturn(Optional.of(mechanic));
        when(distanceService.getAddress(any(), any())).thenReturn("Hà Nội");
        when(rescueOrderRepository.save(any())).thenReturn(saved);
        when(rescueOrderMapper.toResponse(saved)).thenReturn(new RescueOrderResponse());

        service.createRescueOrder(request);

        verify(serviceRepository, never()).findById(any());
        // gửi email với serviceName = null
        verify(emailService).sendNewOrderNotification(any(), any(), any(), isNull());
    }

    @Test
    void createRescueOrder_customerPhoneSameAsMechanic_throwsException() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.MOBILE, "0911111111");

        // customer phone trùng mechanic phone
        CreateRescueOrderRequest request = buildOrderRequest(
                mechanic.getMechanicId(), "0911111111", null);

        when(mechanicRepository.findById(mechanic.getMechanicId())).thenReturn(Optional.of(mechanic));
        when(distanceService.getAddress(any(), any())).thenReturn("Hà Nội");

        assertThrows(RuntimeException.class, () -> service.createRescueOrder(request));
        verify(rescueOrderRepository, never()).save(any());
    }

    @Test
    void createRescueOrder_mechanicNotFound_throwsException() {
        CreateRescueOrderRequest request = buildOrderRequest(UUID.randomUUID(), "0900000001", null);

        when(mechanicRepository.findById(any())).thenReturn(Optional.empty());
        when(distanceService.getAddress(any(), any())).thenReturn("Hà Nội");

        assertThrows(RuntimeException.class, () -> service.createRescueOrder(request));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getMyOrders
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getMyOrders_noOrders_returnsEmpty() {
        when(rescueOrderRepository.findActiveByPhoneToday(any(), any(), any()))
                .thenReturn(List.of());

        List<CustomerOrderResponse> result = service.getMyOrders("0900000001");

        assertTrue(result.isEmpty());
    }

    @Test
    void getMyOrders_mobile_withMechanicAndService_returnsMapped() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.MOBILE, "0911111111");
        UUID serviceId = UUID.randomUUID();

        RescueOrder order = buildOrder(OrderStatus.ACCEPTED);
        order.setMechanicId(mechanic.getMechanicId());
        order.setServiceId(serviceId);

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Cứu hộ ô tô");

        when(rescueOrderRepository.findActiveByPhoneToday(any(), any(), any()))
                .thenReturn(List.of(order));
        when(mechanicRepository.findById(mechanic.getMechanicId()))
                .thenReturn(Optional.of(mechanic));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));

        List<CustomerOrderResponse> result = service.getMyOrders("0900000001");

        assertEquals(1, result.size());
        assertEquals("Nguyen Van A", result.get(0).getMechanicName()); // MOBILE → displayName
        assertEquals("Cứu hộ ô tô", result.get(0).getServiceName());
        assertEquals("ACCEPTED", result.get(0).getStatus());
    }

    @Test
    void getMyOrders_garage_usesGarageName() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.GARAGE, "0911111111");
        mechanic.setGarageName("Garage XYZ");

        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setMechanicId(mechanic.getMechanicId());

        when(rescueOrderRepository.findActiveByPhoneToday(any(), any(), any()))
                .thenReturn(List.of(order));
        when(mechanicRepository.findById(mechanic.getMechanicId()))
                .thenReturn(Optional.of(mechanic));

        List<CustomerOrderResponse> result = service.getMyOrders("0900000001");

        assertEquals("Garage XYZ", result.get(0).getMechanicName()); // GARAGE → garageName
    }

    @Test
    void getMyOrders_noMechanicId_mechanicIsNull() {
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setMechanicId(null); // chưa có thợ

        when(rescueOrderRepository.findActiveByPhoneToday(any(), any(), any()))
                .thenReturn(List.of(order));

        List<CustomerOrderResponse> result = service.getMyOrders("0900000001");

        assertNull(result.get(0).getMechanicName());
        assertNull(result.get(0).getMechanicPhone());
        verify(mechanicRepository, never()).findById(any());
    }

    @Test
    void getMyOrders_noServiceId_serviceNameIsNull() {
        Mechanic mechanic = buildMechanic(MechanicWorkType.MOBILE, "0911111111");

        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setMechanicId(mechanic.getMechanicId());
        order.setServiceId(null); // không có service

        when(rescueOrderRepository.findActiveByPhoneToday(any(), any(), any()))
                .thenReturn(List.of(order));
        when(mechanicRepository.findById(mechanic.getMechanicId()))
                .thenReturn(Optional.of(mechanic));

        List<CustomerOrderResponse> result = service.getMyOrders("0900000001");

        assertNull(result.get(0).getServiceName());
        verify(serviceRepository, never()).findById(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // reportByCustomer
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void reportByCustomer_success() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = buildOrder(OrderStatus.ACCEPTED); // chưa COMPLETED
        order.setOrderId(orderId);

        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(orderId);
        request.setTargetPhone("0911111111");
        request.setContent("Thợ không đến");

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.reportByCustomer(request);

        verify(reportRepository).save(any());
    }

    @Test
    void reportByCustomer_orderAlreadyCompleted_throwsException() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = buildOrder(OrderStatus.COMPLETED);
        order.setOrderId(orderId);

        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CustomException.class, () -> service.reportByCustomer(request));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void reportByCustomer_orderNotFound_throwsException() {
        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(UUID.randomUUID());

        when(rescueOrderRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.reportByCustomer(request));
    }

    @Test
    void reportByCustomer_requestedStatus_success() {
        UUID orderId = UUID.randomUUID();

        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setOrderId(orderId);

        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(orderId);

        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.reportByCustomer(request);

        verify(reportRepository).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // helpers
    // ══════════════════════════════════════════════════════════════════════════

    private SearchMechanicRequestDTO buildSearchRequest() {
        SearchMechanicRequestDTO r = new SearchMechanicRequestDTO();
        r.setLatitude(21.0);
        r.setLongitude(105.0);
        r.setServiceId(UUID.randomUUID());
        r.setType(MechanicType.MOTORBIKE.toString());
        r.setCustomerPhone("0900000001");
        return r;
    }

    private CreateRescueOrderRequest buildOrderRequest(UUID mechanicId, String customerPhone, UUID serviceId) {
        CreateRescueOrderRequest r = new CreateRescueOrderRequest();
        r.setMechanicId(mechanicId);
        r.setCustomerName("Khach Hang");
        r.setCustomerPhone(customerPhone);
        r.setLatitude(21.0);
        r.setLongitude(105.0);
        r.setServiceId(serviceId);
        return r;
    }
}