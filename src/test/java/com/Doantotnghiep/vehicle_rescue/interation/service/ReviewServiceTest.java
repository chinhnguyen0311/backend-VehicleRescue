package com.Doantotnghiep.vehicle_rescue.interation.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.interation.dto.request.CreateReviewRequest;
import com.Doantotnghiep.vehicle_rescue.interation.dto.response.TopMechanicResponse;
import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import com.Doantotnghiep.vehicle_rescue.interation.repository.ReviewRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RescueOrderRepository rescueOrderRepository;

    @Mock
    private MechanicRepository mechanicRepository;

    @InjectMocks
    private ReviewService reviewService;

    private CreateReviewRequest baseReviewRequest;
    private RescueOrder baseOrder;
    private Mechanic baseMechanic;
    private UUID orderId;
    private UUID mechanicId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        mechanicId = UUID.randomUUID();

        baseReviewRequest = new CreateReviewRequest();
        baseReviewRequest.setOrderId(orderId);
        baseReviewRequest.setCustomerPhone("0900000001");
        baseReviewRequest.setRating(5);
        baseReviewRequest.setReview("Tuyệt vời!");

        baseOrder = new RescueOrder();
        baseOrder.setOrderId(orderId);
        baseOrder.setCustomerPhone("0900000001");
        baseOrder.setStatus(OrderStatus.COMPLETED);
        baseOrder.setMechanicId(mechanicId);

        baseMechanic = new Mechanic();
        baseMechanic.setMechanicId(mechanicId);
        baseMechanic.setWorkType(MechanicWorkType.MOBILE);
        Account account = new Account();
        account.setAvatarUrl("avatar.png");
        baseMechanic.setAccount(account);
    }

    // =======================================================================
    // Test createReview
    // =======================================================================

    @Test
    void createReview_success_updatesMechanicRating() {
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(baseOrder));
        when(reviewRepository.existsByOrder_OrderId(orderId)).thenReturn(false);
        when(mechanicRepository.findById(mechanicId)).thenReturn(Optional.of(baseMechanic));

        // Mocking getReviewStats: trả về list chứa 1 array với giá trị là 4.5
        List<Object[]> statsList = Collections.singletonList(new Object[]{4.5});
        when(reviewRepository.getReviewStats(mechanicId)).thenReturn(statsList);

        reviewService.createReview(baseReviewRequest);

        verify(reviewRepository).save(any(Review.class));
        verify(mechanicRepository).save(baseMechanic);

        // Cập nhật điểm dựa trên statsList
        assertEquals(BigDecimal.valueOf(4.5), baseMechanic.getRatingScore());
    }

    @Test
    void createReview_orderNotFound_throwsException() {
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(baseReviewRequest));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_wrongCustomer_throwsException() {
        baseReviewRequest.setCustomerPhone("0999999999"); // Số khác với order
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(baseOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(baseReviewRequest));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void createReview_orderNotCompleted_throwsException() {
        baseOrder.setStatus(OrderStatus.ACCEPTED); // Không phải COMPLETED
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(baseOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(baseReviewRequest));
        assertEquals(ErrorCode.ORDER_INVALID_STATUS, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("hoàn thành"));
    }

    @Test
    void createReview_alreadyReviewed_throwsException() {
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(baseOrder));
        when(reviewRepository.existsByOrder_OrderId(orderId)).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(baseReviewRequest));
        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("đã được đánh giá"));
    }

    @Test
    void createReview_mechanicNotFound_throwsException() {
        when(rescueOrderRepository.findById(orderId)).thenReturn(Optional.of(baseOrder));
        when(reviewRepository.existsByOrder_OrderId(orderId)).thenReturn(false);
        when(mechanicRepository.findById(mechanicId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(baseReviewRequest));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    // =======================================================================
    // Test getTop5Mechanics
    // =======================================================================

    @Test
    void getTop5Mechanics_success_sortsAndMapsCorrectly() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        // 1. Setup Mechanics
        Account acc1 = new Account(); acc1.setAvatarUrl("url1");
        Mechanic m1 = new Mechanic(); m1.setMechanicId(id1); m1.setAccount(acc1); m1.setWorkType(MechanicWorkType.MOBILE); m1.setDisplayName("Name1");

        Account acc2 = new Account(); acc2.setAvatarUrl("url2");
        Mechanic m2 = new Mechanic(); m2.setMechanicId(id2); m2.setAccount(acc2); m2.setWorkType(MechanicWorkType.GARAGE); m2.setGarageName("Garage2");

        Account acc3 = new Account(); acc3.setAvatarUrl("url3");
        Mechanic m3 = new Mechanic(); m3.setMechanicId(id3); m3.setAccount(acc3); m3.setWorkType(MechanicWorkType.MOBILE); m3.setDisplayName("Name3");

        when(mechanicRepository.findAll()).thenReturn(Arrays.asList(m1, m2, m3));

        // 2. Setup reviewStats
        List<Object[]> ratingStats = Arrays.asList(
                new Object[]{id1, 10L, 5.0}, // v=10, R=5.0
                new Object[]{id2, 5L, 4.0}   // v=5, R=4.0
                // id3 v=0
        );
        when(reviewRepository.getMechanicRatingStats()).thenReturn(ratingStats);

        // 3. Setup order stats
        List<Object[]> completedStats = Arrays.asList(
                new Object[]{id1, 20L},
                new Object[]{id2, 10L},
                new Object[]{id3, 0L}
        );
        when(rescueOrderRepository.countCompletedOrders()).thenReturn(completedStats);

        // 4. Setup global average
        when(reviewRepository.getGlobalAverageRating()).thenReturn(4.5); // C = 4.5

        List<TopMechanicResponse> result = reviewService.getTop5Mechanics();

        // Xác minh kết quả
        assertEquals(3, result.size());

        // M1 có score cao nhất -> Vị trí số 1
        TopMechanicResponse top1 = result.get(0);
        assertEquals(id1, top1.getMechanicId());
        assertEquals("Name1", top1.getMechanicName());
        assertEquals(5.0, top1.getAvgRating());
        assertEquals(10L, top1.getTotalReviews());
        assertEquals(20L, top1.getTotalCompleted());

        // M2 Garage -> Vị trí số 2
        TopMechanicResponse top2 = result.get(1);
        assertEquals(id2, top2.getMechanicId());
        assertEquals("Garage2", top2.getMechanicName()); // Garage nên dùng GarageName
        assertEquals(4.0, top2.getAvgRating());
        assertEquals(10L, top2.getTotalCompleted());

        // M3 (Chưa có review nào, v=0) -> Vị trí cuối (Score = -1)
        TopMechanicResponse top3 = result.get(2);
        assertEquals(id3, top3.getMechanicId());
        assertEquals(0.0, top3.getAvgRating());
        assertEquals(0L, top3.getTotalReviews());
    }

    @Test
    void getTop5Mechanics_nullGlobalAverage_handlesSafely() {
        // Test trường hợp chưa có bất kì đánh giá nào trên hệ thống (Ctmp = null)
        when(mechanicRepository.findAll()).thenReturn(Collections.singletonList(baseMechanic));
        when(reviewRepository.getMechanicRatingStats()).thenReturn(Collections.emptyList());
        when(rescueOrderRepository.countCompletedOrders()).thenReturn(Collections.emptyList());
        when(reviewRepository.getGlobalAverageRating()).thenReturn(null);

        List<TopMechanicResponse> result = reviewService.getTop5Mechanics();

        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).getAvgRating()); // Score sẽ là -1 do v=0, xếp bét
    }
}