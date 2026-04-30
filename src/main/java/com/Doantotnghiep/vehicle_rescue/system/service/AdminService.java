package com.Doantotnghiep.vehicle_rescue.system.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountStatus;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.dto.PageResponse;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicOrderHistoryResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicSubscription;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.SubscriptionStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicSubscriptionRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.ServiceRepository;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.system.entity.Report;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
import com.Doantotnghiep.vehicle_rescue.system.enums.ReportedByType;
import com.Doantotnghiep.vehicle_rescue.system.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminService {
    private final RescueOrderRepository rescueOrderRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepository;
    private final MechanicSubscriptionRepository subscriptionRepository;
    private final ReportRepository reportRepository;
    private final EmailService emailService;

    public AdminService(RescueOrderRepository rescueOrderRepository,
                        MechanicRepository mechanicRepository,
                        ServiceRepository serviceRepository,
                        ReviewRepository reviewRepository,
                        AccountRepository accountRepository,
                        MechanicSubscriptionRepository subscriptionRepository,
                        ReportRepository reportRepository,
                        @Lazy EmailService emailService) {
        this.rescueOrderRepository = rescueOrderRepository;
        this.mechanicRepository = mechanicRepository;
        this.serviceRepository = serviceRepository;
        this.reviewRepository = reviewRepository;
        this.accountRepository = accountRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.reportRepository = reportRepository;
        this.emailService = emailService;
    }
    public AdminDashboardResponse getDashboard() {

        long totalCompleted = rescueOrderRepository.countByStatus(OrderStatus.COMPLETED);

        long totalRequested = rescueOrderRepository.count(); // hoặc count theo status != CANCEL

        long totalMechanics = mechanicRepository.count();

        double avgPerMonth = calculateAvgOrdersPerMonth();

        List<MonthlyStats> monthlyStats = getMonthlyStats();

        List<ServiceStats> serviceRanking = getServiceRanking();

        List<MechanicStats> topMechanics = getTop5Mechanics();

        return AdminDashboardResponse.builder()
                .totalCompletedOrders(totalCompleted)
                .totalRequestedOrders(totalRequested)
                .totalMechanics(totalMechanics)
                .avgOrdersPerMonth(avgPerMonth)
                .monthlyStats(monthlyStats)
                .serviceRanking(serviceRanking)
                .topMechanics(topMechanics)
                .build();
    }
    private double calculateAvgOrdersPerMonth() {
        List<RescueOrder> orders = rescueOrderRepository.findAll();

        if (orders.isEmpty()) return 0;

        LocalDate first = orders.stream()
                .map(o -> o.getCreatedAt().toLocalDate())
                .min(LocalDate::compareTo)
                .get();

        LocalDate now = LocalDate.now();

        long months = ChronoUnit.MONTHS.between(first, now) + 1;

        return (double) orders.size() / months;
    }
    private List<MonthlyStats> getMonthlyStats() {

        Map<String, List<RescueOrder>> grouped = rescueOrderRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(o ->
                        o.getCreatedAt().getYear() + "-" +
                                String.format("%02d", o.getCreatedAt().getMonthValue())
                ));

        List<MonthlyStats> result = new ArrayList<>();

        for (String month : grouped.keySet()) {

            List<RescueOrder> orders = grouped.get(month);

            long completed = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .count();

            result.add(MonthlyStats.builder()
                    .month(month)
                    .completed(completed)
                    .requested(orders.size())
                    .build());
        }

        return result.stream()
                .sorted(Comparator.comparing(MonthlyStats::getMonth))
                .toList();
    }
    private List<ServiceStats> getServiceRanking() {

        Map<String, Long> map = rescueOrderRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        o -> {
                            if (o.getServiceId() == null) return "Không xác định";

                            String name = serviceRepository.getServiceName(o.getServiceId());
                            return name != null ? name : "Không xác định";
                        },
                        Collectors.counting()
                ));

        return map.entrySet().stream()
                .map(e -> ServiceStats.builder()
                        .serviceName(e.getKey())
                        .totalRequests(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getTotalRequests(), a.getTotalRequests()))
                .toList();
    }
    private List<MechanicStats> getTop5Mechanics() {

        List<Mechanic> mechanics = mechanicRepository.findAll();

        return mechanics.stream()
                .map(m -> {

                    List<RescueOrder> orders =
                            rescueOrderRepository.findByMechanicIdAndStatus(
                                    m.getMechanicId(), OrderStatus.COMPLETED);

                    long totalCompleted = orders.size();

                    double avgRating = orders.stream()
                            .map(order -> reviewRepository.findByOrderId(order.getOrderId())
                                    .map(Review::getRating)
                                    .orElse(null))
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0);

                    return MechanicStats.builder()
                            .mechanicName(m.getAccount().getFullName())
                            .avgRating(avgRating)
                            .totalCompleted(totalCompleted)
                            .build();
                })
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.getAvgRating(), a.getAvgRating());
                    if (cmp == 0) {
                        return Long.compare(b.getTotalCompleted(), a.getTotalCompleted());
                    }
                    return cmp;
                })
                .limit(5)
                .toList();
    }
    public List<MechanicAdminResponse> getAllMechanicsForAdmin() {
        return mechanicRepository.getAllMechanicStats()
                .stream()
                .map(row -> MechanicAdminResponse.builder()
                        .mechanicId((UUID) row[0])
                        .mechanicName((String) row[1])
                        .mechanicPhone((String) row[2])
                        .mechanicEmail((String) row[3])
                        .avgRating(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0)
                        .totalCompleted(row[5] != null ? ((Number) row[5]).longValue() : 0L)
                        .createdAt(toLocalDateTime(row[6]))
                        .expiredAt(toLocalDateTime(row[7]))
                        .isActive((Boolean) row[8])
                        .workType((String) row[9])
                        .garageName((String) row[10])
                        .garageAddress((String) row[11])
                        .avatarUrl((String) row[12])
                        .build()
                )
                .toList();
    }
    public void banAccount(UUID mechanicId) {
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mechanic"));
        Account account = mechanic.getAccount();
        account.setIsActive(false);
        account.setStatus(AccountStatus.BANNED);
        account.setBannedAt(LocalDateTime.now());
        accountRepository.save(account);
    }
    public List<PendingAccountResponse> getPendingAccounts() {
        return accountRepository.getPendingAccounts(AccountStatus.PENDING);
    }

    public void approveAccount(UUID accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getStatus() != AccountStatus.PENDING) {
            throw new RuntimeException("Tài khoản không ở trạng thái PENDING");
        }

        account.setIsActive(true);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        mechanic.setIsActiveSubs(true);
        mechanic.setSubsEndDate(OffsetDateTime.now().plusDays(30));
        mechanic.setStatus(MechanicStatus.OFFLINE);

        mechanicRepository.save(mechanic);
        emailService.sendAccountApprovalNotification(
                account.getEmail(),
                mechanic.getDisplayName()
        );
    }

    public void rejectAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getStatus() != AccountStatus.PENDING) {
            throw new RuntimeException("Chỉ được xoá account PENDING");
        }
        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        emailService.sendAccountRejectionNotification(
                account.getEmail(),
                mechanic.getDisplayName()
        );
        accountRepository.delete(account);
    }

    public List<SubscriptionResponse> getPendingSubscriptions() {
        return subscriptionRepository.getAllSubscriptions(SubscriptionStatus.PENDING);
    }
    public void approveSubscription(UUID subscriptionId) {
        MechanicSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (sub.getStatus() != SubscriptionStatus.PENDING) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_INVALID_STATUS);
        }

        Mechanic mechanic = sub.getMechanic();
        Account account = mechanic.getAccount();
        mechanic.setSubsEndDate(sub.getNewEndDate());
        mechanic.setIsActiveSubs(true);

        sub.setStatus(SubscriptionStatus.APPROVED);

        mechanicRepository.save(mechanic);
        subscriptionRepository.save(sub);
        emailService.sendSubscriptionRenewalNotification(
                account.getEmail(),
                mechanic.getDisplayName(),
                sub.getNewEndDate()
        );
    }
    public void rejectSubscription(UUID subscriptionId) {

        MechanicSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (sub.getStatus() != SubscriptionStatus.PENDING) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_INVALID_STATUS);
        }
        Mechanic mechanic = sub.getMechanic();
        mechanic.setIsActiveSubs(false);
        sub.setStatus(SubscriptionStatus.REJECTED);
        subscriptionRepository.save(sub);
    }
    public List<ReportAdminResponse> getAllReportsForAdmin() {

        List<Report> reports = reportRepository.findAllOrderByCreatedAtDesc();

        return reports.stream().map(r -> {

            String reporterName;
            String target;

            RescueOrder order = null;

            if (r.getOrderId() != null) {
                order = rescueOrderRepository.findById(r.getOrderId()).orElse(null);
            }

            // 👤 Reporter
            if (r.getReportedByType() == ReportedByType.MECHANIC) {

                Mechanic mechanic = mechanicRepository
                        .findByPhoneNumber(r.getReporterPhone())
                        .orElse(null);

                if (mechanic != null) {
                    reporterName = mechanic.getWorkType() == MechanicWorkType.GARAGE
                            ? mechanic.getGarageName()
                            : mechanic.getDisplayName();
                } else {
                    reporterName = r.getReporterPhone();
                }

            } else {
                // CUSTOMER → lấy từ ORDER
                reporterName = (order != null)
                        ? order.getCustomerName()
                        : r.getReporterPhone();
            }

            // 🎯 Target
            if (r.getReportedTarget() == RelatedType.SYSTEM) {
                target = "Hệ thống";

            } else if (r.getReportedTarget() == RelatedType.MECHANIC) {

                Mechanic mechanic = mechanicRepository
                        .findByPhoneNumber(r.getTargetPhone())
                        .orElse(null);

                if (mechanic != null) {
                    target = mechanic.getWorkType() == MechanicWorkType.GARAGE
                            ? mechanic.getGarageName()
                            : mechanic.getDisplayName();
                } else {
                    target = r.getTargetPhone();
                }

            } else {
                // CUSTOMER → lấy từ ORDER
                target = (order != null)
                        ? order.getCustomerName()
                        : r.getTargetPhone();
            }

            return ReportAdminResponse.builder()
                    .reportId(r.getReportId())
                    .reason(r.getReasonCategory().name())
                    .reportedByType(r.getReportedByType())
                    .reporterName(reporterName)
                    .target(target)
                    .content(r.getContent())
                    .createdAt(r.getCreatedAt())
                    .build();
        }).toList();
    }
    public PageResponse<OrderHistoryResponse> getAllOrdersForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<RescueOrder> orderPage = rescueOrderRepository.findAll(pageable);

        List<OrderHistoryResponse> content = orderPage.getContent().stream().map(order -> {

            // service name
            String serviceName = "Không xác định";

            if (order.getServiceId() != null) {
                serviceName = serviceRepository.findById(order.getServiceId())
                        .map(service -> service.getName())
                        .orElse("Không tìm thấy dịch vụ");
            }

            // review
            Integer rating = reviewRepository
                    .findByOrderId(order.getOrderId())
                    .map(Review::getRating)
                    .orElse(null);

            String review = reviewRepository
                    .findByOrderId(order.getOrderId())
                    .map(Review::getReview)
                    .orElse(null);
            String mechanicPhone;
            mechanicPhone = mechanicRepository.findById(order.getMechanicId())
                    .map(m -> m.getPhoneNumber())
                    .orElse(null);
            OffsetDateTime displayTime = switch (order.getStatus()) {
                case REQUESTED -> order.getCreatedAt();
                case COMPLETED -> order.getCompletedAt();
                default -> order.getUpdatedAt();
            };

            return OrderHistoryResponse.builder()
                    .orderId(order.getOrderId())
                    .customerName(order.getCustomerName())
                    .phone(order.getCustomerPhone())
                    .serviceName(serviceName)
                    .mechanicName(order.getMechanicName())
                    .mechanicPhone(mechanicPhone)
                    .status(order.getStatus().name())
                    .address(order.getCustomerAddress())
                    .completedAt(displayTime)
                    .rating(rating)
                    .review(review)
                    .build();

        }).toList();

        return PageResponse.<OrderHistoryResponse>builder()
                .content(content)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .empty(orderPage.isEmpty())
                .numberOfElements(orderPage.getNumberOfElements())
                .sorted(orderPage.getSort().isSorted())
                .build();
    }
    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (obj instanceof LocalDateTime ldt) return ldt;
        return null;
    }
}