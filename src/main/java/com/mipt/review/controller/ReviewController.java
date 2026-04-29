package com.mipt.review.controller;

import com.mipt.review.dto.CreateReviewRequest;
import com.mipt.review.dto.UpdateReviewRequest;
import com.mipt.review.dto.ReviewResponse;
import com.mipt.review.dto.SellerRatingResponse;
import com.mipt.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Создать отзыв
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader("X-User-Id") UUID userId) {

        log.info("POST /api/v1/reviews - creating review");
        ReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Обновить отзыв
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @RequestHeader("X-User-Id") UUID userId) {

        log.info("PUT /api/v1/reviews/{} - updating review", reviewId);
        ReviewResponse response = reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(response);
    }

    // Удалить отзыв
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader("X-User-Id") UUID userId) {

        log.info("DELETE /api/v1/reviews/{} - deleting review", reviewId);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    // Получить отзывы о продавце
    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<Page<ReviewResponse>> getSellerReviews(
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer rating) {

        log.info("GET /api/v1/reviews/sellers/{}", sellerId);

        Page<ReviewResponse> reviews = reviewService.getReviewsBySeller(
                sellerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")),
                rating
        );

        return ResponseEntity.ok(reviews);
    }

    // Получить отзывы об объявлении
    @GetMapping("/advertisements/{advertisementId}")
    public ResponseEntity<Page<ReviewResponse>> getAdvertisementReviews(
            @PathVariable UUID advertisementId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/reviews/advertisements/{}", advertisementId);

        Page<ReviewResponse> reviews = reviewService.getReviewsByAdvertisement(
                advertisementId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(reviews);
    }

    // Получить отзыв по ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        log.info("GET /api/v1/reviews/{}", reviewId);
        ReviewResponse response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }

    // Получить статистику продавца
    @GetMapping("/sellers/{sellerId}/rating")
    public ResponseEntity<SellerRatingResponse> getSellerRating(@PathVariable UUID sellerId) {
        log.info("GET /api/v1/reviews/sellers/{}/rating", sellerId);
        SellerRatingResponse rating = reviewService.getSellerRating(sellerId);
        return ResponseEntity.ok(rating);
    }
}