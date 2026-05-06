package com.Doantotnghiep.vehicle_rescue.system.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountStatus;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.common.dto.PageResponse;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicSubscription;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.SubscriptionStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicSubscriptionRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.ServiceRepository;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.system.entity.Report;
import com.Doantotnghiep.vehicle_rescue.system.enums.ReasonCategory;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
import com.Doantotnghiep.vehicle_rescue.system.enums.ReportedByType;
import com.Doantotnghiep.vehicle_rescue.system.enums.ReportStatus;
import com.Doantotnghiep.vehicle_rescue.system.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminServiceTest {

    @InjectMocks
    private AdminService service;

    @Mock private RescueOrderRepository rescueOrderRepository;
    @Mock private MechanicRepository mechanicRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private MechanicSubscriptionRepository subscriptionRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private EmailService emailService;

    // ─── shared fixture builders ───────────────────────────────────────────────

    private Account buildAccount(AccountStatus status) {
        Account a = new Account();
        a.setUsername("user1");
        a.setEmail("user@example.com");
        a.setStatus(status);
        a.setIsActive(status == AccountStatus.ACTIVE);
        return a;
    }

    private Mechanic buildMechanic(Account account, MechanicWorkType workType) {
        Mechanic m = new Mechanic();
        m.setMechanicId(UUID.randomUUID());
        m.setAccount(account);
        m.setWorkType(workType);
        m.setType(MechanicType.GENERAL);
        m.setDisplayName("Nguyen Van A");
        m.setGarageName("Garage ABC");
        m.setPhoneNumber("0911111111");
        return m;
    }

    private RescueOrder buildOrder(OrderStatus status) {
        RescueOrder o = new RescueOrder();
        o.setOrderId(UUID.randomUUID());
        o.setStatus(status);
        o.setCustomerName("Khach Hang");
        o.setCustomerPhone("0900000001");
        o.setMechanicName("Thợ A");
        o.setCreatedAt(OffsetDateTime.now());
        o.setUpdatedAt(OffsetDateTime.now());
        o.setCompletedAt(status == OrderStatus.COMPLETED ? OffsetDateTime.now() : null);
        return o;
    }

    private Report buildReport(ReportedByType byType, RelatedType targetType) {
        Report r = new Report();
        r.setReportId(UUID.randomUUID());
        r.setReportedByType(byType);
        r.setReportedTarget(targetType);
        r.setReporterPhone("0911111111");
        r.setTargetPhone("0922222222");
        r.setReasonCategory(ReasonCategory.MECHANIC_NO_COME);
        r.setContent("Nội dung báo cáo");
        r.setStatus(ReportStatus.PENDING);
        r.setCreatedAt(OffsetDateTime.now());
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getDashboard
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getDashboard_noOrders_returnsZeroAvg() {
        when(rescueOrderRepository.countByStatus(OrderStatus.COMPLETED)).thenReturn(0L);
        when(rescueOrderRepository.count()).thenReturn(0L);
        when(mechanicRepository.count()).thenReturn(3L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of()); // calculateAvgOrdersPerMonth + getMonthlyStats + getServiceRanking
        when(mechanicRepository.findAll()).thenReturn(List.of());    // getTop5Mechanics

        AdminDashboardResponse result = service.getDashboard();

        assertNotNull(result);
        assertEquals(0L, result.getTotalCompletedOrders());
        assertEquals(0.0, result.getAvgOrdersPerMonth());
        assertTrue(result.getMonthlyStats().isEmpty());
        assertTrue(result.getTopMechanics().isEmpty());
    }

    @Test
    void getDashboard_withOrders_calculatesCorrectly() {
        RescueOrder completed = buildOrder(OrderStatus.COMPLETED);
        completed.setCreatedAt(OffsetDateTime.now().minusMonths(1));
        RescueOrder requested = buildOrder(OrderStatus.REQUESTED);

        // getDashboard gọi findAll() 3 lần (calculateAvgOrdersPerMonth, getMonthlyStats, getServiceRanking)
        when(rescueOrderRepository.countByStatus(OrderStatus.COMPLETED)).thenReturn(1L);
        when(rescueOrderRepository.count()).thenReturn(2L);
        when(mechanicRepository.count()).thenReturn(1L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of(completed, requested));
        when(mechanicRepository.findAll()).thenReturn(List.of());

        AdminDashboardResponse result = service.getDashboard();

        assertEquals(1L, result.getTotalCompletedOrders());
        assertEquals(2L, result.getTotalRequestedOrders());
        assertTrue(result.getAvgOrdersPerMonth() > 0);
        assertFalse(result.getMonthlyStats().isEmpty());
    }

    @Test
    void getDashboard_withServiceId_buildsServiceRanking() {
        UUID serviceId = UUID.randomUUID();
        RescueOrder order = buildOrder(OrderStatus.COMPLETED);
        order.setServiceId(serviceId);

        when(rescueOrderRepository.countByStatus(any())).thenReturn(1L);
        when(rescueOrderRepository.count()).thenReturn(1L);
        when(mechanicRepository.count()).thenReturn(0L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of(order));
        when(serviceRepository.getServiceName(serviceId)).thenReturn("Cứu hộ xe máy");
        when(mechanicRepository.findAll()).thenReturn(List.of());

        AdminDashboardResponse result = service.getDashboard();

        assertFalse(result.getServiceRanking().isEmpty());
        assertEquals("Cứu hộ xe máy", result.getServiceRanking().get(0).getServiceName());
    }

    @Test
    void getDashboard_serviceIdNull_usesDefaultName() {
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setServiceId(null);

        when(rescueOrderRepository.countByStatus(any())).thenReturn(0L);
        when(rescueOrderRepository.count()).thenReturn(1L);
        when(mechanicRepository.count()).thenReturn(0L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of(order));
        when(mechanicRepository.findAll()).thenReturn(List.of());

        AdminDashboardResponse result = service.getDashboard();

        assertEquals("Không xác định", result.getServiceRanking().get(0).getServiceName());
    }

    @Test
    void getDashboard_serviceNameNull_usesDefaultName() {
        UUID serviceId = UUID.randomUUID();
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setServiceId(serviceId);

        when(rescueOrderRepository.countByStatus(any())).thenReturn(0L);
        when(rescueOrderRepository.count()).thenReturn(1L);
        when(mechanicRepository.count()).thenReturn(0L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of(order));
        when(serviceRepository.getServiceName(serviceId)).thenReturn(null); // null → "Không xác định"
        when(mechanicRepository.findAll()).thenReturn(List.of());

        AdminDashboardResponse result = service.getDashboard();

        assertEquals("Không xác định", result.getServiceRanking().get(0).getServiceName());
    }

    @Test
    void getDashboard_top5Mechanics_sortsByRatingThenCompleted() {
        Account acc1 = buildAccount(AccountStatus.ACTIVE);
        acc1.setFullName("Thợ 1");
        Account acc2 = buildAccount(AccountStatus.ACTIVE);
        acc2.setFullName("Thợ 2");

        Mechanic m1 = buildMechanic(acc1, MechanicWorkType.MOBILE);
        Mechanic m2 = buildMechanic(acc2, MechanicWorkType.MOBILE);

        RescueOrder o1 = buildOrder(OrderStatus.COMPLETED);
        RescueOrder o2 = buildOrder(OrderStatus.COMPLETED);

        Review review1 = new Review(); review1.setRating(5);
        Review review2 = new Review(); review2.setRating(3);

        when(rescueOrderRepository.countByStatus(any())).thenReturn(2L);
        when(rescueOrderRepository.count()).thenReturn(2L);
        when(mechanicRepository.count()).thenReturn(2L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of());
        when(mechanicRepository.findAll()).thenReturn(List.of(m1, m2));
        when(rescueOrderRepository.findByMechanicIdAndStatus(m1.getMechanicId(), OrderStatus.COMPLETED))
                .thenReturn(List.of(o1));
        when(rescueOrderRepository.findByMechanicIdAndStatus(m2.getMechanicId(), OrderStatus.COMPLETED))
                .thenReturn(List.of(o2));
        when(reviewRepository.findByOrderId(o1.getOrderId())).thenReturn(Optional.of(review1));
        when(reviewRepository.findByOrderId(o2.getOrderId())).thenReturn(Optional.of(review2));

        AdminDashboardResponse result = service.getDashboard();

        assertEquals(2, result.getTopMechanics().size());
        // Thợ 1 rating=5 phải đứng trước Thợ 2 rating=3
        assertEquals("Thợ 1", result.getTopMechanics().get(0).getMechanicName());
    }

    @Test
    void getDashboard_mechanicNoReview_avgRatingZero() {
        Account acc = buildAccount(AccountStatus.ACTIVE);
        acc.setFullName("Thợ Mới");
        Mechanic m = buildMechanic(acc, MechanicWorkType.MOBILE);
        RescueOrder o = buildOrder(OrderStatus.COMPLETED);

        when(rescueOrderRepository.countByStatus(any())).thenReturn(1L);
        when(rescueOrderRepository.count()).thenReturn(1L);
        when(mechanicRepository.count()).thenReturn(1L);
        when(rescueOrderRepository.findAll()).thenReturn(List.of());
        when(mechanicRepository.findAll()).thenReturn(List.of(m));
        when(rescueOrderRepository.findByMechanicIdAndStatus(m.getMechanicId(), OrderStatus.COMPLETED))
                .thenReturn(List.of(o));
        when(reviewRepository.findByOrderId(o.getOrderId())).thenReturn(Optional.empty());

        AdminDashboardResponse result = service.getDashboard();

        assertEquals(0.0, result.getTopMechanics().get(0).getAvgRating());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllMechanicsForAdmin
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getAllMechanicsForAdmin_empty_returnsEmptyList() {
        when(mechanicRepository.getAllMechanicStats()).thenReturn(List.of());

        List<MechanicAdminResponse> result = service.getAllMechanicsForAdmin();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllMechanicsForAdmin_withRows_mapsCorrectly() {
        UUID mechId = UUID.randomUUID();
        Object[] row = new Object[]{
                mechId,           // [0] mechanicId
                "Nguyen Van A",   // [1] name
                "0911111111",     // [2] phone
                "a@a.com",        // [3] email
                4.5,              // [4] avgRating
                10L,              // [5] totalCompleted
                null,             // [6] createdAt (LocalDateTime) → null ok
                null,             // [7] expiredAt
                true,             // [8] isActive
                "MOBILE",         // [9] workType
                null,             // [10] garageName
                null,             // [11] garageAddress
                "http://avatar"   // [12] avatarUrl
        };

        when(mechanicRepository.getAllMechanicStats()).thenReturn(Collections.singletonList(row));

        List<MechanicAdminResponse> result = service.getAllMechanicsForAdmin();

        assertEquals(1, result.size());
        assertEquals(mechId, result.get(0).getMechanicId());
        assertEquals(4.5, result.get(0).getAvgRating());
        assertEquals(10L, result.get(0).getTotalCompleted());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void getAllMechanicsForAdmin_nullRatingAndCompleted_defaultsToZero() {
        Object[] row = new Object[]{
                UUID.randomUUID(), "A", "0911", "a@a.com",
                null,  // avgRating null → 0.0
                null,  // totalCompleted null → 0L
                null, null, false, "MOBILE", null, null, null
        };

        when(mechanicRepository.getAllMechanicStats()).thenReturn(Collections.singletonList(row));

        List<MechanicAdminResponse> result = service.getAllMechanicsForAdmin();

        assertEquals(0.0, result.get(0).getAvgRating());
        assertEquals(0L, result.get(0).getTotalCompleted());
    }

    @Test
    void getAllMechanicsForAdmin_timestampCreatedAt_convertsCorrectly() {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(LocalDateTime.of(2024, 1, 15, 10, 0));
        Object[] row = new Object[]{
                UUID.randomUUID(), "A", "0911", "a@a.com",
                null, null,
                ts,   // [6] createdAt là Timestamp
                null,
                true, "MOBILE", null, null, null
        };

        when(mechanicRepository.getAllMechanicStats()).thenReturn(Collections.singletonList(row));

        List<MechanicAdminResponse> result = service.getAllMechanicsForAdmin();

        assertNotNull(result.get(0).getCreatedAt());
        assertEquals(2024, result.get(0).getCreatedAt().getYear());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // banAccount
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void banAccount_success() {
        Account account = buildAccount(AccountStatus.ACTIVE);
        Mechanic mechanic = buildMechanic(account, MechanicWorkType.MOBILE);

        when(mechanicRepository.findById(mechanic.getMechanicId())).thenReturn(Optional.of(mechanic));

        service.banAccount(mechanic.getMechanicId());

        assertEquals(AccountStatus.BANNED, account.getStatus());
        assertFalse(account.getIsActive());
        assertNotNull(account.getBannedAt());
        verify(accountRepository).save(account);
    }

    @Test
    void banAccount_mechanicNotFound_throwsException() {
        when(mechanicRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.banAccount(UUID.randomUUID()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getPendingAccounts
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getPendingAccounts_delegatesToRepository() {
        when(accountRepository.getPendingAccounts(AccountStatus.PENDING)).thenReturn(List.of());

        List<PendingAccountResponse> result = service.getPendingAccounts();

        assertNotNull(result);
        verify(accountRepository).getPendingAccounts(AccountStatus.PENDING);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // approveAccount
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void approveAccount_success() {
        Account account = buildAccount(AccountStatus.PENDING);
        Mechanic mechanic = buildMechanic(account, MechanicWorkType.MOBILE);
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(mechanicRepository.findByAccount(account)).thenReturn(Optional.of(mechanic));

        service.approveAccount(accountId);

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.getIsActive());
        assertEquals(MechanicStatus.OFFLINE, mechanic.getStatus());
        assertTrue(mechanic.getIsActiveSubs());
        assertNotNull(mechanic.getSubsEndDate());
        verify(mechanicRepository).save(mechanic);
        verify(emailService).sendAccountApprovalNotification(anyString(), anyString());
    }

    @Test
    void approveAccount_notPendingStatus_throwsException() {
        Account account = buildAccount(AccountStatus.ACTIVE); // đã ACTIVE, không phải PENDING
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> service.approveAccount(accountId));
        verify(mechanicRepository, never()).save(any());
    }

    @Test
    void approveAccount_accountNotFound_throwsException() {
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.approveAccount(UUID.randomUUID()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // rejectAccount
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void rejectAccount_success() {
        Account account = buildAccount(AccountStatus.PENDING);
        Mechanic mechanic = buildMechanic(account, MechanicWorkType.MOBILE);
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(mechanicRepository.findByAccount(account)).thenReturn(Optional.of(mechanic));

        service.rejectAccount(accountId);

        verify(emailService).sendAccountRejectionNotification(anyString(), anyString());
        verify(mechanicRepository).delete(mechanic);
        verify(accountRepository).delete(account);
    }

    @Test
    void rejectAccount_notPendingStatus_throwsException() {
        Account account = buildAccount(AccountStatus.ACTIVE);
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> service.rejectAccount(accountId));
        verify(mechanicRepository, never()).delete(any());
    }

    @Test
    void rejectAccount_accountNotFound_throwsException() {
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.rejectAccount(UUID.randomUUID()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getPendingSubscriptions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getPendingSubscriptions_delegatesToRepository() {
        when(subscriptionRepository.getAllSubscriptions(SubscriptionStatus.PENDING)).thenReturn(List.of());

        List<SubscriptionResponse> result = service.getPendingSubscriptions();

        assertNotNull(result);
        verify(subscriptionRepository).getAllSubscriptions(SubscriptionStatus.PENDING);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // approveSubscription
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void approveSubscription_success() {
        Account account = buildAccount(AccountStatus.ACTIVE);
        Mechanic mechanic = buildMechanic(account, MechanicWorkType.MOBILE);

        MechanicSubscription sub = new MechanicSubscription();
        sub.setStatus(SubscriptionStatus.PENDING);
        sub.setMechanic(mechanic);
        sub.setNewEndDate(OffsetDateTime.now().plusDays(30));

        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));

        service.approveSubscription(subId);

        assertEquals(SubscriptionStatus.APPROVED, sub.getStatus());
        assertTrue(mechanic.getIsActiveSubs());
        assertEquals(sub.getNewEndDate(), mechanic.getSubsEndDate());
        verify(mechanicRepository).save(mechanic);
        verify(subscriptionRepository).save(sub);
        verify(emailService).sendSubscriptionRenewalNotification(anyString(), anyString(), any());
    }

    @Test
    void approveSubscription_notPending_throwsException() {
        MechanicSubscription sub = new MechanicSubscription();
        sub.setStatus(SubscriptionStatus.APPROVED); // đã approved

        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));

        assertThrows(CustomException.class, () -> service.approveSubscription(subId));
        verify(mechanicRepository, never()).save(any());
    }

    @Test
    void approveSubscription_notFound_throwsException() {
        when(subscriptionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.approveSubscription(UUID.randomUUID()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // rejectSubscription
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void rejectSubscription_success() {
        Account account = buildAccount(AccountStatus.ACTIVE);
        Mechanic mechanic = buildMechanic(account, MechanicWorkType.MOBILE);

        MechanicSubscription sub = new MechanicSubscription();
        sub.setStatus(SubscriptionStatus.PENDING);
        sub.setMechanic(mechanic);

        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));

        service.rejectSubscription(subId);

        assertEquals(SubscriptionStatus.REJECTED, sub.getStatus());
        assertFalse(mechanic.getIsActiveSubs());
        verify(subscriptionRepository).save(sub);
    }

    @Test
    void rejectSubscription_notPending_throwsException() {
        MechanicSubscription sub = new MechanicSubscription();
        sub.setStatus(SubscriptionStatus.REJECTED);

        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));

        assertThrows(CustomException.class, () -> service.rejectSubscription(subId));
    }

    @Test
    void rejectSubscription_notFound_throwsException() {
        when(subscriptionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.rejectSubscription(UUID.randomUUID()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllReportsForAdmin
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getAllReports_empty_returnsEmptyList() {
        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of());

        assertTrue(service.getAllReportsForAdmin().isEmpty());
    }

    @Test
    void getAllReports_reportedByMechanic_mobile_targetMechanic_mobile() {
        Report report = buildReport(ReportedByType.MECHANIC, RelatedType.MECHANIC);
        report.setOrderId(null);

        Account acc = buildAccount(AccountStatus.ACTIVE);
        Mechanic reporter = buildMechanic(acc, MechanicWorkType.MOBILE); // MOBILE → displayName
        Mechanic target   = buildMechanic(acc, MechanicWorkType.MOBILE);

        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(report));
        when(mechanicRepository.findByPhoneNumber(report.getReporterPhone()))
                .thenReturn(Optional.of(reporter));
        when(mechanicRepository.findByPhoneNumber(report.getTargetPhone()))
                .thenReturn(Optional.of(target));

        List<ReportAdminResponse> result = service.getAllReportsForAdmin();

        assertEquals(1, result.size());
        assertEquals(reporter.getDisplayName(), result.get(0).getReporterName());
        assertEquals(target.getDisplayName(), result.get(0).getTarget());
    }

    @Test
    void getAllReports_reportedByMechanic_garage_targetGarage() {
        Report report = buildReport(ReportedByType.MECHANIC, RelatedType.MECHANIC);
        report.setOrderId(null);

        Account acc = buildAccount(AccountStatus.ACTIVE);
        Mechanic reporter = buildMechanic(acc, MechanicWorkType.GARAGE); // GARAGE → garageName
        Mechanic target   = buildMechanic(acc, MechanicWorkType.GARAGE);

        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(report));
        when(mechanicRepository.findByPhoneNumber(report.getReporterPhone()))
                .thenReturn(Optional.of(reporter));
        when(mechanicRepository.findByPhoneNumber(report.getTargetPhone()))
                .thenReturn(Optional.of(target));

        List<ReportAdminResponse> result = service.getAllReportsForAdmin();

        assertEquals(reporter.getGarageName(), result.get(0).getReporterName());
        assertEquals(target.getGarageName(), result.get(0).getTarget());
    }

    @Test
    void getAllReports_mechanicReporterNotFound_usesPhone() {
        Report report = buildReport(ReportedByType.MECHANIC, RelatedType.MECHANIC);
        report.setOrderId(null);

        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(report));
        when(mechanicRepository.findByPhoneNumber(report.getReporterPhone()))
                .thenReturn(Optional.empty()); // không tìm thấy → dùng phone
        when(mechanicRepository.findByPhoneNumber(report.getTargetPhone()))
                .thenReturn(Optional.empty());

        List<ReportAdminResponse> result = service.getAllReportsForAdmin();

        assertEquals(report.getReporterPhone(), result.get(0).getReporterName());
        assertEquals(report.getTargetPhone(), result.get(0).getTarget());
    }

    @Test
    void getAllReports_reportedByCustomer_withOrder_targetSystem() {
        UUID orderId = UUID.randomUUID();
        Report report = buildReport(ReportedByType.CUSTOMER, RelatedType.SYSTEM);
        report.setOrderId(orderId);

        RescueOrder order = buildOrder(OrderStatus.COMPLETED);
        order.setOrderId(orderId);
        order.setCustomerName("Khach VIP");

        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(report));
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        List<ReportAdminResponse> result = service.getAllReportsForAdmin();

        assertEquals("Khach VIP", result.get(0).getReporterName()); // customer → order.customerName
        assertEquals("Hệ thống", result.get(0).getTarget());       // SYSTEM → "Hệ thống"
    }

    @Test
    void getAllReports_reportedByCustomer_noOrder_targetCustomer() {
        Report report = buildReport(ReportedByType.CUSTOMER, RelatedType.CUSTOMER);
        report.setOrderId(null); // không có order

        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(report));

        List<ReportAdminResponse> result = service.getAllReportsForAdmin();

        // order null → dùng phone
        assertEquals(report.getReporterPhone(), result.get(0).getReporterName());
        assertEquals(report.getTargetPhone(), result.get(0).getTarget());
    }

    @Test
    void getAllReports_reportedByCustomer_withOrder_targetCustomer() {
        UUID orderId = UUID.randomUUID();
        Report report = buildReport(ReportedByType.CUSTOMER, RelatedType.CUSTOMER);
        report.setOrderId(orderId);

        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setOrderId(orderId);
        order.setCustomerName("Khach B");

        when(reportRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(report));
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        List<ReportAdminResponse> result = service.getAllReportsForAdmin();

        assertEquals("Khach B", result.get(0).getTarget()); // CUSTOMER → order.customerName
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllOrdersForAdmin
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getAllOrdersForAdmin_empty_returnsEmptyPage() {
        Page<RescueOrder> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getAllOrdersForAdmin_requestedOrder_usesCreatedAt() {
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setMechanicId(UUID.randomUUID());

        Page<RescueOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(reviewRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.empty());
        when(mechanicRepository.findById(order.getMechanicId())).thenReturn(Optional.empty());

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        assertEquals(1, result.getContent().size());
        // REQUESTED → displayTime = createdAt
        assertEquals(order.getCreatedAt(), result.getContent().get(0).getCompletedAt());
    }

    @Test
    void getAllOrdersForAdmin_completedOrder_usesCompletedAt() {
        RescueOrder order = buildOrder(OrderStatus.COMPLETED);
        order.setMechanicId(UUID.randomUUID());
        order.setServiceId(UUID.randomUUID());

        com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service svc =
                new com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service();
        svc.setName("Vá xe");

        Review review = new Review();
        review.setRating(5);
        review.setReview("Tốt");

        Page<RescueOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(serviceRepository.findById(order.getServiceId())).thenReturn(Optional.of(svc));
        when(reviewRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(review));
        when(mechanicRepository.findById(order.getMechanicId())).thenReturn(Optional.empty());

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        OrderHistoryResponse resp = result.getContent().get(0);
        assertEquals(order.getCompletedAt(), resp.getCompletedAt()); // COMPLETED → completedAt
        assertEquals(5, resp.getRating());
        assertEquals("Tốt", resp.getReview());
        assertEquals("Vá xe", resp.getServiceName());
    }

    @Test
    void getAllOrdersForAdmin_acceptedOrder_usesUpdatedAt() {
        RescueOrder order = buildOrder(OrderStatus.ACCEPTED);
        order.setMechanicId(UUID.randomUUID());
        order.setUpdatedAt(OffsetDateTime.now());

        Page<RescueOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(reviewRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.empty());
        when(mechanicRepository.findById(order.getMechanicId())).thenReturn(Optional.empty());

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        // default branch → updatedAt
        assertEquals(order.getUpdatedAt(), result.getContent().get(0).getCompletedAt());
    }

    @Test
    void getAllOrdersForAdmin_noServiceId_usesDefaultName() {
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setServiceId(null);
        order.setMechanicId(UUID.randomUUID());

        Page<RescueOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(reviewRepository.findByOrderId(any())).thenReturn(Optional.empty());
        when(mechanicRepository.findById(any())).thenReturn(Optional.empty());

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        assertEquals("Không xác định", result.getContent().get(0).getServiceName());
    }

    @Test
    void getAllOrdersForAdmin_serviceNotFound_usesNotFoundName() {
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setServiceId(UUID.randomUUID());
        order.setMechanicId(UUID.randomUUID());

        Page<RescueOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(serviceRepository.findById(order.getServiceId())).thenReturn(Optional.empty());
        when(reviewRepository.findByOrderId(any())).thenReturn(Optional.empty());
        when(mechanicRepository.findById(any())).thenReturn(Optional.empty());

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        assertEquals("Không tìm thấy dịch vụ", result.getContent().get(0).getServiceName());
    }

    @Test
    void getAllOrdersForAdmin_mechanicFound_includesPhone() {
        RescueOrder order = buildOrder(OrderStatus.REQUESTED);
        order.setMechanicId(UUID.randomUUID());

        Account acc = buildAccount(AccountStatus.ACTIVE);
        Mechanic mechanic = buildMechanic(acc, MechanicWorkType.MOBILE);
        mechanic.setPhoneNumber("0988888888");

        Page<RescueOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(rescueOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(reviewRepository.findByOrderId(any())).thenReturn(Optional.empty());
        when(mechanicRepository.findById(order.getMechanicId())).thenReturn(Optional.of(mechanic));

        PageResponse<OrderHistoryResponse> result = service.getAllOrdersForAdmin(0, 10);

        assertEquals("0988888888", result.getContent().get(0).getMechanicPhone());
    }
}