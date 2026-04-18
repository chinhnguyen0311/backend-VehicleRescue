package com.Doantotnghiep.vehicle_rescue.interation.repository;

import com.Doantotnghiep.vehicle_rescue.interation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @Query("SELECT r FROM Review r WHERE r.order.orderId = :orderId")
    Optional<Review> findByOrderId(@Param("orderId") UUID orderId);
    Optional<Review> findByOrder_OrderId(UUID orderId);

    boolean existsByOrder_OrderId(UUID orderId);
    @Query("""
SELECT AVG(r.rating)
FROM Review r
WHERE r.mechanic.mechanicId = :mechanicId
""")
    Double getAverageRating(UUID mechanicId);

    @Query("""
SELECT COUNT(r)
FROM Review r
WHERE r.mechanic.mechanicId = :mechanicId
""")
    Long countReviews(UUID mechanicId);

    @Query("""
SELECT r
FROM Review r
WHERE r.mechanic.mechanicId = :mechanicId
ORDER BY r.createdAt DESC
LIMIT 3
""")
    List<Review> findTop3Recent(UUID mechanicId);

    @Query("""
SELECT m.mechanicId,
       COUNT(r.reviewId),
       AVG(r.rating)
FROM Mechanic m
LEFT JOIN Review r ON r.mechanic.mechanicId = m.mechanicId
GROUP BY m.mechanicId
""")
    List<Object[]> getMechanicStats();

    @Query("""
SELECT r.mechanic.mechanicId, COUNT(r.reviewId), AVG(r.rating)
FROM Review r
GROUP BY r.mechanic.mechanicId
""")
    List<Object[]> getMechanicRatingStats();
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getGlobalAverageRating();

    @Query("""
SELECT AVG(r.rating), COUNT(r.reviewId)
FROM Review r
WHERE r.mechanic.mechanicId = :mechanicId
""")
    List<Object[]> getReviewStats(@Param("mechanicId") UUID mechanicId);
}
