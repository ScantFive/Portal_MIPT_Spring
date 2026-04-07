package com.mipt.review.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID advertisementId;
    private String advertisementName;
    private UUID sellerId;
    private String sellerName;
    private UUID buyerId;
    private String buyerName;  // Если isAnonymous = true, то "Аноним"
    private Integer rating;
    private String comment;
    private boolean isVerifiedPurchase;
    private boolean isAnonymous;
    private Instant createdAt;
    private Instant updatedAt;  // Добавляем для информации об изменении
}