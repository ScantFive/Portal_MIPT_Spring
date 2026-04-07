package com.mipt.review.service;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.repository.AdvertisementRepository;
import com.mipt.review.dto.CreateReviewRequest;
import com.mipt.review.dto.ReviewResponse;
import com.mipt.review.dto.SellerRatingResponse;
import com.mipt.review.dto.UpdateReviewRequest;
import com.mipt.review.event.ReviewEvent;
import com.mipt.review.model.Review;
import com.mipt.review.repository.ReviewRepository;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AdvertisementRepository advertisementRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.topic.review-events:review-events}")
    private String TOPIC_REVIEW_EVENTS;

    // ==================== CREATE ====================

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, UUID buyerId) {
        log.info("Creating review for seller: {} by buyer: {}", request.getSellerId(), buyerId);

        // Проверка: нельзя оставить отзыв самому себе
        if (request.getSellerId().equals(buyerId)) {
            throw new IllegalArgumentException("Нельзя оставить отзыв самому себе");
        }

        String advertisementName = null;

        // Проверка: если указано объявление - проверяем автора
        if (request.getAdvertisementId() != null) {
            Advertisement advertisement = advertisementRepository.findById(request.getAdvertisementId())
                    .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + request.getAdvertisementId()));

            if (!advertisement.getAuthorId().equals(request.getSellerId())) {
                throw new IllegalArgumentException(
                        String.format("Продавец %s не является автором объявления %s",
                                request.getSellerId(), request.getAdvertisementId())
                );
            }

            advertisementName = advertisement.getName();
        }

        // Проверка: нельзя оставить два одинаковых отзыва
        boolean alreadyReviewed = reviewRepository.existsBySellerIdAndBuyerIdAndAdvertisementId(
                request.getSellerId(), buyerId, request.getAdvertisementId()
        );

        if (alreadyReviewed) {
            throw new IllegalStateException("Вы уже оставили отзыв на это объявление/продавца");
        }

        // Создаём отзыв
        Review review = Review.builder()
                .advertisementId(request.getAdvertisementId())
                .sellerId(request.getSellerId())
                .buyerId(buyerId)
                .rating(request.getRating())
                .comment(request.getComment())
                .isVerifiedPurchase(false)
                .isAnonymous(request.isAnonymous())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created successfully with id: {}", saved.getId());

        // Отправляем событие в Kafka
        sendReviewEvent(saved, "REVIEW_CREATED");

        // Обновляем рейтинг продавца
        updateAndSendSellerRating(saved.getSellerId());

        return mapToResponse(saved, advertisementName);
    }

    // ==================== UPDATE ====================

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request, UUID userId) {
        log.info("Updating review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

        // Проверка: только автор может изменить отзыв
        if (!review.getBuyerId().equals(userId)) {
            throw new SecurityException("Вы можете изменять только свои отзывы");
        }

        // Опционально: ограничение по времени (24 часа)
        Instant timeLimit = review.getCreatedAt().plus(24, ChronoUnit.HOURS);
        if (Instant.now().isAfter(timeLimit)) {
            throw new IllegalStateException("Отзыв можно изменить только в течение 24 часов после создания");
        }

        // Обновляем поля
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        if (request.getIsAnonymous() != null) {
            review.setAnonymous(request.getIsAnonymous());
        }

        Review updated = reviewRepository.save(review);
        log.info("Review updated successfully: {}", reviewId);

        // Отправляем событие в Kafka
        sendReviewEvent(updated, "REVIEW_UPDATED");

        // Обновляем рейтинг продавца
        updateAndSendSellerRating(review.getSellerId());

        // Получаем название объявления
        AtomicReference<String> advertisementName = new AtomicReference<>();
        if (review.getAdvertisementId() != null) {
            advertisementRepository.findById(review.getAdvertisementId())
                    .ifPresent(ad -> advertisementName.set(ad.getName()));
        }

        return mapToResponse(updated, advertisementName.get());
    }

    // ==================== DELETE ====================

    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        log.info("Deleting review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

        // Проверка: только автор может удалить отзыв
        if (!review.getBuyerId().equals(userId)) {
            throw new SecurityException("Вы можете удалять только свои отзывы");
        }

        UUID sellerId = review.getSellerId();
        Integer rating = review.getRating();

        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", reviewId);

        // Отправляем событие в Kafka
        ReviewEvent event = ReviewEvent.builder()
                .eventType("REVIEW_DELETED")
                .reviewId(reviewId)
                .sellerId(sellerId)
                .buyerId(userId)
                .rating(rating)
                .build();

        log.info("📤 Sending review deleted event to Kafka: {}", event);
        kafkaTemplate.send(TOPIC_REVIEW_EVENTS, reviewId.toString(), event);

        // Обновляем рейтинг продавца
        updateAndSendSellerRating(sellerId);
    }

    // ==================== GET ====================

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsBySeller(UUID sellerId, Pageable pageable, Integer rating) {
        log.debug("Getting reviews for seller: {}", sellerId);

        Page<Review> reviews;
        if (rating != null) {
            reviews = reviewRepository.findBySellerIdAndRating(sellerId, rating, pageable);
        } else {
            reviews = reviewRepository.findBySellerId(sellerId, pageable);
        }

        return reviews.map(review -> mapToResponse(review, null));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByAdvertisement(UUID advertisementId, Pageable pageable) {
        log.debug("Getting reviews for advertisement: {}", advertisementId);

        Page<Review> reviews = reviewRepository.findByAdvertisementId(advertisementId, pageable);

        String adName = advertisementRepository.findById(advertisementId)
                .map(Advertisement::getName)
                .orElse(null);

        return reviews.map(review -> mapToResponse(review, adName));
    }

    @Transactional(readOnly = true)
    public SellerRatingResponse getSellerRating(UUID sellerId) {
        log.debug("Getting rating for seller: {}", sellerId);

        Double averageRating = reviewRepository.getAverageRatingBySellerId(sellerId);
        Integer totalReviews = reviewRepository.getTotalReviewsCount(sellerId);

        if (totalReviews == null || totalReviews == 0) {
            return SellerRatingResponse.builder()
                    .sellerId(sellerId)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .fiveStarCount(0)
                    .fourStarCount(0)
                    .threeStarCount(0)
                    .twoStarCount(0)
                    .oneStarCount(0)
                    .build();
        }

        return SellerRatingResponse.builder()
                .sellerId(sellerId)
                .averageRating(Math.round(averageRating * 10) / 10.0)
                .totalReviews(totalReviews)
                .fiveStarCount(reviewRepository.getRatingCount(sellerId, 5))
                .fourStarCount(reviewRepository.getRatingCount(sellerId, 4))
                .threeStarCount(reviewRepository.getRatingCount(sellerId, 3))
                .twoStarCount(reviewRepository.getRatingCount(sellerId, 2))
                .oneStarCount(reviewRepository.getRatingCount(sellerId, 1))
                .build();
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        log.debug("Getting review by id: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

        AtomicReference<String> advertisementName = new AtomicReference<>();
        if (review.getAdvertisementId() != null) {
            advertisementRepository.findById(review.getAdvertisementId())
                    .ifPresent(ad -> advertisementName.set(ad.getName()));
        }

        return mapToResponse(review, advertisementName.get());
    }

    // ==================== KAFKA EVENTS ====================

    private void sendReviewEvent(Review review, String eventType) {
        ReviewEvent event = ReviewEvent.builder()
                .eventType(eventType)
                .reviewId(review.getId())
                .sellerId(review.getSellerId())
                .buyerId(review.getBuyerId())
                .advertisementId(review.getAdvertisementId())
                .rating(review.getRating())
                .comment(review.getComment())
                .isAnonymous(review.isAnonymous())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();

        log.info("📤 Sending review event to Kafka: eventType={}, reviewId={}", eventType, review.getId());
        kafkaTemplate.send(TOPIC_REVIEW_EVENTS, review.getId().toString(), event);
    }

    private void updateAndSendSellerRating(UUID sellerId) {
        Double averageRating = reviewRepository.getAverageRatingBySellerId(sellerId);
        Integer totalReviews = reviewRepository.getTotalReviewsCount(sellerId);

        if (totalReviews == null || totalReviews == 0) {
            log.debug("No reviews found for seller: {}, skipping rating update", sellerId);
            return;
        }

        ReviewEvent ratingEvent = ReviewEvent.builder()
                .eventType("SELLER_RATING_UPDATED")
                .sellerId(sellerId)
                .averageRating(Math.round(averageRating * 10) / 10.0)
                .totalReviews(totalReviews)
                .fiveStarCount(reviewRepository.getRatingCount(sellerId, 5))
                .fourStarCount(reviewRepository.getRatingCount(sellerId, 4))
                .threeStarCount(reviewRepository.getRatingCount(sellerId, 3))
                .twoStarCount(reviewRepository.getRatingCount(sellerId, 2))
                .oneStarCount(reviewRepository.getRatingCount(sellerId, 1))
                .updatedAt(Instant.now())
                .build();

        log.info("📤 Sending seller rating event to Kafka: sellerId={}, avgRating={}, total={}",
                sellerId, ratingEvent.getAverageRating(), totalReviews);
        kafkaTemplate.send(TOPIC_REVIEW_EVENTS, sellerId.toString(), ratingEvent);
    }

    // ==================== MAPPER ====================

    private ReviewResponse mapToResponse(Review review, String advertisementName) {
        String buyerName = review.isAnonymous() ? "Аноним" : review.getBuyerId().toString().substring(0, 8);

        return ReviewResponse.builder()
                .id(review.getId())
                .advertisementId(review.getAdvertisementId())
                .advertisementName(advertisementName)
                .sellerId(review.getSellerId())
                .sellerName(review.getSellerId().toString().substring(0, 8))
                .buyerId(review.getBuyerId())
                .buyerName(buyerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .isAnonymous(review.isAnonymous())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}