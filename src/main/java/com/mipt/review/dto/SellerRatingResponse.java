package com.mipt.review.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerRatingResponse {
    private UUID sellerId;
    private String sellerName;
    private Double averageRating;
    private Integer totalReviews;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
}