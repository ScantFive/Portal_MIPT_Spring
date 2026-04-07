package com.mipt.review.repository;

import com.mipt.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Отзывы о продавце (все)
    List<Review> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);

    // Отзывы о продавце с пагинацией
    Page<Review> findBySellerId(UUID sellerId, Pageable pageable);

    // Отзывы о продавце с определённым рейтингом
    Page<Review> findBySellerIdAndRating(UUID sellerId, Integer rating, Pageable pageable);

    // Отзывы об объявлении
    Page<Review> findByAdvertisementId(UUID advertisementId, Pageable pageable);

    // Проверка, оставлял ли пользователь уже отзыв на это объявление/продавца
    boolean existsBySellerIdAndBuyerIdAndAdvertisementId(UUID sellerId, UUID buyerId, UUID advertisementId);

    // Статистика рейтинга продавца
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sellerId = :sellerId")
    Double getAverageRatingBySellerId(@Param("sellerId") UUID sellerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.sellerId = :sellerId")
    Integer getTotalReviewsCount(@Param("sellerId") UUID sellerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.sellerId = :sellerId AND r.rating = :rating")
    Integer getRatingCount(@Param("sellerId") UUID sellerId, @Param("rating") Integer rating);
}