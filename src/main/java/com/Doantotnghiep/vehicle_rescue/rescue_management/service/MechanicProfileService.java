package com.Doantotnghiep.vehicle_rescue.rescue_management.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.interation.dto.response.MechanicRankingDTO;
import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.LocationMessage;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateProfileRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicService;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicServiceId;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicServiceRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.ServiceRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MechanicProfileService {
    private final AccountRepository accountRepository;
    private final MechanicRepository mechanicRepository;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final MechanicServiceRepository mechanicServiceRepository;
    private final DistanceService distanceService;
    private final RescueOrderRepository rescueOrderRepository;
    private final ReviewRepository reviewRepository;
    private final ServiceRepository serviceRepository;
    public ProfileResponseDTO getProfile() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        ProfileResponseDTO.ProfileResponseDTOBuilder builder = ProfileResponseDTO.builder()
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .type(mechanic.getType())
                .workType(mechanic.getWorkType())
                .subsEndDate(mechanic.getSubsEndDate());

        if (mechanic.getWorkType() == MechanicWorkType.GARAGE) {
            builder
                    .garageName(mechanic.getGarageName())
                    .garageAddress(mechanic.getGarageAddress());
        }
        return builder.build();
    }
    public ProfileResponseDTO updateProfile(UpdateProfileRequestDTO request){
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Cập nhật Account
        account.setFullName(request.getFullName());
        account.setPhoneNumber(request.getPhoneNumber());
        accountRepository.save(account);

        // Cập nhật Mechanic
        mechanic.setType(request.getType());
        mechanic.setDisplayName(request.getFullName());
        mechanic.setPhoneNumber(request.getPhoneNumber());

        // Nếu là GARAGE thì validate và cập nhật thông tin garage
        if (mechanic.getWorkType() == MechanicWorkType.GARAGE) {
            mechanic.setGarageName(request.getGarageName());
            mechanic.setGarageAddress(request.getGarageAddress());

            if (request.getGarageLatitude() != null && request.getGarageLongitude() != null) {
                Point garageLocation = GEOMETRY_FACTORY.createPoint(
                        new Coordinate(request.getGarageLongitude(), request.getGarageLatitude())
                );
                mechanic.setGarageLocation(garageLocation);
            }
        }

        mechanicRepository.save(mechanic);
        return getProfile();
    }
    public void addService(AddMechanicServiceRequestDTO request) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (mechanicServiceRepository.existsByMechanicIdAndServiceId(
                mechanic.getMechanicId(), request.getServiceId())) {
            throw new CustomException(ErrorCode.SERVICE_ALREADY_EXISTS);
        }

        MechanicService mechanicService = MechanicService.builder()
                .mechanicId(mechanic.getMechanicId())
                .serviceId(request.getServiceId())
                .customPrice(request.getCustomPrice())
                .build();

        mechanicServiceRepository.save(mechanicService);
    }
    public List<MechanicServiceResponseDTO> getServices() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return mechanicServiceRepository.findServicesByMechanicId(mechanic.getMechanicId());
    }
    public void updateServicePrice(UUID serviceId, UpdateMechanicServiceRequestDTO request) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        MechanicService mechanicService = mechanicServiceRepository
                .findById(new MechanicServiceId(mechanic.getMechanicId(), serviceId))
                .orElseThrow(() -> new CustomException(ErrorCode.SERVICE_NOT_FOUND));

        mechanicService.setCustomPrice(request.getCustomPrice());
        mechanicServiceRepository.save(mechanicService);
    }
    public List<MechanicOrderItemResponse> getRequestedOrdersForMechanic() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Double mechanicLat = extractLat(
                mechanic.getWorkType().name().equals("GARAGE")
                        ? mechanic.getGarageLocation()
                        : mechanic.getCurrentLocation()
        );

        Double mechanicLng = extractLng(
                mechanic.getWorkType().name().equals("GARAGE")
                        ? mechanic.getGarageLocation()
                        : mechanic.getCurrentLocation()
        );

        List<RescueOrder> orders = rescueOrderRepository
                .findByMechanicIdAndStatus(mechanic.getMechanicId(), OrderStatus.REQUESTED);

        return orders.stream().map(order -> {

            Double cusLat = extractLat(order.getCustomerLocation());
            Double cusLng = extractLng(order.getCustomerLocation());

            Double distance = distanceService.getDistance(
                    mechanicLat, mechanicLng,
                    cusLat, cusLng
            );

            String address = distanceService.getAddress(cusLat, cusLng);
            String serviceName = "Không xác định";
            if (order.getServiceId() != null) {
                serviceName = serviceRepository.findById(order.getServiceId())
                        .map(com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service::getName)
                        .orElse("Không tìm thấy dịch vụ");
            }
            return MechanicOrderItemResponse.builder()
                    .orderId(order.getOrderId())
                    .customerName(order.getCustomerName())
                    .customerPhone(order.getCustomerPhone())
                    .serviceName(serviceName)
                    .latitude(cusLat)
                    .longitude(cusLng)
                    .distance(distance)
                    .address(address)
                    .createdAt(order.getCreatedAt())
                    .build();

        }).toList();
    }
    public void acceptOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));


        boolean hasActiveOrder = rescueOrderRepository
                .existsByMechanicIdAndStatusIn(
                        mechanic.getMechanicId(),
                        List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS)
                );

        if (hasActiveOrder) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_IN_PROGRESS);
        }

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.REQUESTED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.ACCEPTED);
        rescueOrderRepository.save(order);
    }

    public void cancelOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // Chỉ cancel khi đang REQUESTED hoặc ACCEPTED
        if (order.getStatus() != OrderStatus.REQUESTED
                && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.CANCELLED);
        rescueOrderRepository.save(order);
    }
    public void inProcessOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        rescueOrderRepository.save(order);
    }
    public void completeOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(OffsetDateTime.now());
        rescueOrderRepository.save(order);
    }
    public MechanicOrderItemResponse getCurrentOrder() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Optional<RescueOrder> optionalOrder = rescueOrderRepository
                .findFirstByMechanicIdAndStatusIn(
                        mechanic.getMechanicId(),
                        List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS)
                );


        if (optionalOrder.isEmpty()) {
            return null;
        }

        RescueOrder order = optionalOrder.get();

        Double mechanicLat = extractLat(
                mechanic.getWorkType().name().equals("GARAGE")
                        ? mechanic.getGarageLocation()
                        : mechanic.getCurrentLocation()
        );

        Double mechanicLng = extractLng(
                mechanic.getWorkType().name().equals("GARAGE")
                        ? mechanic.getGarageLocation()
                        : mechanic.getCurrentLocation()
        );

        Double cusLat = extractLat(order.getCustomerLocation());
        Double cusLng = extractLng(order.getCustomerLocation());

        Double distance = distanceService.getDistance(
                mechanicLat, mechanicLng,
                cusLat, cusLng
        );

        String address = distanceService.getAddress(cusLat, cusLng);

        String serviceName = "Không xác định";
        if (order.getServiceId() != null) {
            serviceName = serviceRepository.findById(order.getServiceId())
                    .map(com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service::getName)
                    .orElse("Không tìm thấy dịch vụ");
        }
        return MechanicOrderItemResponse.builder()
                .orderId(order.getOrderId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .serviceName(serviceName)
                .latitude(cusLat)
                .longitude(cusLng)
                .distance(distance)
                .address(address)
                .createdAt(order.getCreatedAt())
                .build();
    }
    public List<MechanicOrderHistoryResponse> getOrderHistory() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<RescueOrder> orders =
                rescueOrderRepository.findByMechanicIdOrderByCreatedAtDesc(mechanic.getMechanicId());

        return orders.stream().map(order -> {
            String serviceName = "Không xác định";
            if (order.getServiceId() != null) {
                serviceName = serviceRepository.findById(order.getServiceId())
                        .map(com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service::getName)
                        .orElse("Không tìm thấy dịch vụ");
            }
            Integer rating = reviewRepository
                    .findByOrderId(order.getOrderId())
                    .map(Review::getRating)
                    .orElse(null);
            String review = reviewRepository
                    .findByOrderId(order.getOrderId())
                    .map(Review::getReview)
                    .orElse(null);
            return MechanicOrderHistoryResponse.builder()
                    .orderId(order.getOrderId())
                    .customerName(order.getCustomerName())
                    .phone(order.getCustomerPhone())
                    .serviceName(serviceName)
                    .status(order.getStatus().name())
                    .address(order.getCustomerAddress())
                    .completedAt(order.getCompletedAt())
                    .rating(rating)
                    .review(review)
                    .build();

        }).toList();
    }
    public void updateLocationRealtime(LocationMessage request, Principal principal) {

        if (principal == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        // ✅ LẤY USER TỪ PRINCIPAL
        String username = principal.getName();

        System.out.println("👉 Username từ WS: " + username);

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!mechanic.getWorkType().name().equals("MOBILE")) return;
        System.out.println("Principal: " + principal);
        System.out.println("Username: " + principal.getName());
        Point point = GEOMETRY_FACTORY.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())
        );

        mechanic.setCurrentLocation(point);
        mechanicRepository.save(mechanic);
    }
    public MechanicStatisticResponse getStatistic() {

        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        UUID mechanicId = mechanic.getMechanicId();

        // ✅ số ca hoàn thành
        int totalCompleted = rescueOrderRepository.countCompleted(mechanicId);

        // ✅ rating trung bình
        Double avgRating = reviewRepository.getAverageRating(mechanicId);
        Long totalReviews = reviewRepository.countReviews(mechanicId);

        if (avgRating == null) avgRating = 0.0;
        if (totalReviews == null) totalReviews = 0L;

        // ✅ số ca trung bình mỗi tháng
        OffsetDateTime firstOrderDate = rescueOrderRepository.getFirstOrderDate(mechanicId);

        double avgPerMonth = 0;
        if (firstOrderDate != null) {
            long months = java.time.Duration.between(firstOrderDate, OffsetDateTime.now()).toDays() / 30;
            if (months == 0) months = 1;
            avgPerMonth = (double) totalCompleted / months;
        }

        // ✅ ranking (Bayesian)
        int rank = getMechanicRank(mechanic.getMechanicId());

        // ✅ 3 review gần nhất
        List<Review> reviews = reviewRepository.findTop3Recent(mechanicId);

        List<MechanicStatisticResponse.RecentReviewItem> reviewItems =
                reviews.stream().map(r -> {

                    RescueOrder order = r.getOrder();

                    String serviceName = null;
                    if (order.getServiceId() != null) {
                        serviceName = serviceRepository.findById(order.getServiceId())
                                .map(s -> s.getName())
                                .orElse(null);
                    }

                    return MechanicStatisticResponse.RecentReviewItem.builder()
                            .customerName(order.getCustomerName())
                            .rating(r.getRating())
                            .review(r.getReview())
                            .serviceName(serviceName)
                            .createdAt(r.getCreatedAt().toString())
                            .build();

                }).toList();

        return MechanicStatisticResponse.builder()
                .totalCompletedOrders(totalCompleted)
                .averageRating(BigDecimal.valueOf(avgRating))
                .avgOrdersPerMonth(avgPerMonth)
                .rankingScore(rank)
                .recentReviews(reviewItems)
                .build();
    }
    public int getMechanicRank(UUID currentMechanicId) {

        List<Object[]> stats = reviewRepository.getMechanicStats();

        // C = rating trung bình toàn hệ thống
        double C = stats.stream()
                .mapToDouble(s -> s[2] != null ? ((Number) s[2]).doubleValue() : 0)
                .average()
                .orElse(0);

        int m = 5; // min reviews

        List<MechanicRankingDTO> rankingList = new ArrayList<>();

        for (Object[] s : stats) {
            UUID mechanicId = (UUID) s[0];
            int v = ((Number) s[1]).intValue();
            double R = s[2] != null ? ((Number) s[2]).doubleValue() : 0;

            double score = (v / (double)(v + m)) * R
                    + (m / (double)(v + m)) * C;

            rankingList.add(new MechanicRankingDTO(mechanicId, score));
        }

        // sort giảm dần
        rankingList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // tìm thứ hạng
        for (int i = 0; i < rankingList.size(); i++) {
            if (rankingList.get(i).getMechanicId().equals(currentMechanicId)) {
                return i + 1; // rank bắt đầu từ 1
            }
        }

        return -1;
    }
    public MechanicDetailResponse getMechanicDetail(UUID mechanicId) {

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 🔹 Lấy tên
        String mechanicName = mechanic.getWorkType().name().equals("GARAGE")
                ? mechanic.getGarageName()
                : mechanic.getDisplayName();

        // 🔹 Lấy review stats
        List<Object[]> statsList = reviewRepository.getReviewStats(mechanicId);

        Double avgRating = 0.0;
        Long totalReviews = 0L;

        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0);

            avgRating = stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
            totalReviews = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
        }

        // 🔹 Address chỉ có nếu GARAGE
        String address = null;
        if (mechanic.getWorkType().name().equals("GARAGE")) {
            address = mechanic.getGarageAddress();
        }

        return MechanicDetailResponse.builder()
                .mechanicName(mechanicName)
                .mechanicPhone(mechanic.getPhoneNumber())
                .avgRating(avgRating)
                .totalReviews(totalReviews)
                .type(mechanic.getType().name())
                .workType(mechanic.getWorkType().name())
                .address(address)
                .build();
    }
    private Double extractLat(Object pointObj) {
        if (pointObj == null) return null;

        if (pointObj instanceof org.locationtech.jts.geom.Point p) {
            return p.getY();
        }

        if (pointObj instanceof org.geolatte.geom.Point p) {
            return p.getPosition().getCoordinate(1); // lat
        }

        return null;
    }

    private Double extractLng(Object pointObj) {
        if (pointObj == null) return null;

        if (pointObj instanceof org.locationtech.jts.geom.Point p) {
            return p.getX();
        }

        if (pointObj instanceof org.geolatte.geom.Point p) {
            return p.getPosition().getCoordinate(0); // lng
        }

        return null;
    }

}
