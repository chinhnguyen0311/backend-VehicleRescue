package com.Doantotnghiep.vehicle_rescue.interation.service;

import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.interation.dto.request.CreateReviewRequest;
import com.Doantotnghiep.vehicle_rescue.interation.dto.response.TopMechanicResponse;
import com.Doantotnghiep.vehicle_rescue.interation.dto.response.TopMechanicTemp;
import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final RescueOrderRepository rescueOrderRepository;
    private final MechanicRepository mechanicRepository;
    public void createReview(CreateReviewRequest request) {

        RescueOrder order = rescueOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // ❗ check đúng customer
        if (!order.getCustomerPhone().equals(request.getCustomerPhone())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // ❗ chỉ cho review khi COMPLETED
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS,
                    "Chỉ có thể đánh giá khi đơn đã hoàn thành");
        }

        // ❗ check đã review chưa
        if (reviewRepository.existsByOrder_OrderId(order.getOrderId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST,
                    "Đơn này đã được đánh giá");
        }

        Mechanic mechanic = mechanicRepository.findById(order.getMechanicId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Review review = Review.builder()
                .order(order)
                .mechanic(mechanic)
                .rating(request.getRating())
                .review(request.getReview())
                .build();

        reviewRepository.save(review);
    }
    public List<TopMechanicResponse> getTop5Mechanics() {

        List<Object[]> ratingStats = reviewRepository.getMechanicRatingStats();
        List<Object[]> completedStats = rescueOrderRepository.countCompletedOrders();
        List<Mechanic> mechanics = mechanicRepository.findAll();

        Map<UUID, Long> reviewCountMap = new HashMap<>();
        Map<UUID, Double> avgRatingMap = new HashMap<>();

        for (Object[] r : ratingStats) {
            UUID mechanicId = (UUID) r[0];
            Long count = (Long) r[1];
            Double avg = r[2] != null ? ((Number) r[2]).doubleValue() : 0;

            reviewCountMap.put(mechanicId, count);
            avgRatingMap.put(mechanicId, avg);
        }

        Map<UUID, Long> completedMap = new HashMap<>();
        for (Object[] c : completedStats) {
            UUID mechanicId = (UUID) c[0];
            Long total = (Long) c[1];
            completedMap.put(mechanicId, total);
        }

        // ✅ FIX C
        Double Ctmp = reviewRepository.getGlobalAverageRating();
        double C = Ctmp != null ? Ctmp : 0;

        int m = 5;

        List<TopMechanicTemp> tempList = new ArrayList<>();

        for (Mechanic mcn : mechanics) {
            UUID id = mcn.getMechanicId();

            long v = reviewCountMap.getOrDefault(id, 0L);
            double R = avgRatingMap.getOrDefault(id, 0.0);

            double score;

            // ✅ FIX v = 0
            if (v == 0) {
                score = -1;
            } else {
                score = (v / (double)(v + m)) * R
                        + (m / (double)(v + m)) * C;
            }

            tempList.add(new TopMechanicTemp(
                    mcn,
                    score,
                    R,
                    v,
                    completedMap.getOrDefault(id, 0L)
            ));
        }

        // ✅ FIX SORT
        tempList.sort(
                Comparator.comparing(TopMechanicTemp::getScore)
                        .thenComparing(TopMechanicTemp::getTotalCompleted)
                        .reversed()
        );

        return tempList.stream()
                .limit(5)
                .map(t -> {
                    Mechanic mcn = t.getMechanic();

                    String name = mcn.getWorkType().name().equals("GARAGE")
                            ? mcn.getGarageName()
                            : mcn.getDisplayName();

                    return TopMechanicResponse.builder()
                            .mechanicId(mcn.getMechanicId())
                            .mechanicName(name)
                            .avgRating(t.getAvgRating())
                            .totalReviews(t.getTotalReviews())
                            .totalCompleted(t.getTotalCompleted())
                            .build();
                })
                .toList();
    }
}
