package com.mipt.advertisement.controller.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvertisementResponse {
    private UUID id;
    private String type;
    private String typeDisplayName;
    private String status;
    private UUID authorId;
    private String name;
    private String description;
    private Long price;
    private Set<String> photoUrls;
    private String category;
    private String categoryDisplayName;
    private boolean isFavorite;
    private Instant createdAt;
}