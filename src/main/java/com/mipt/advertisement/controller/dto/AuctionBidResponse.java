package com.mipt.advertisement.controller.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AuctionBidResponse {
    private UUID id;
    private UUID advertisementId;
    private UUID bidderId;
    private Long amount;
    private Instant createdAt;
}
