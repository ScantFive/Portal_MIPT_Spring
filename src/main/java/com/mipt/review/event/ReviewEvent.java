package com.mipt.review.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEvent {
    private String eventType;          // REVIEW_CREATED, REVIEW_UPDATED, REVIEW_DELETED, SELLER_RATING_UPDATED
    private UUID reviewId;
    private UUID sellerId;
    private UUID buyerId;
    private UUID advertisementId;
    private Integer rating;
    private String comment;
    private Boolean isAnonymous;
    private Boolean isVerifiedPurchase;
    private Instant createdAt;
    private Instant updatedAt;

    // Для обновления рейтинга
    private Double averageRating;
    private Integer totalReviews;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
}