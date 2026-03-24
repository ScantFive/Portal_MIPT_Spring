package com.mipt.controller;

import com.mipt.controller.dto.AdvertisementRequest;
import com.mipt.controller.dto.AdvertisementResponse;
import com.mipt.model.advertisement.Advertisement;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;
import java.time.Instant;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AdvertisementMapper {

    public Advertisement toEntity(AdvertisementRequest request) {
        Category category = Category.fromDisplayName(request.getCategory());
        if (category == null) {
            // Пробуем найти по enum name
            try {
                category = Category.valueOf(request.getCategory());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Некорректная категория: " + request.getCategory());
            }
        }

        return Advertisement.builder()
            .id(UUID.randomUUID())
            .type(request.getType())
            .authorId(request.getAuthorId())
            .name(request.getName())
            .description(request.getDescription())
            .createdAt(Instant.now())
            .status(AdvertisementStatus.DRAFT)
            .photoUrls(request.getPhotoUrls() != null ?
                request.getPhotoUrls() : new TreeSet<>())
            .category(category)
            .price(request.getPrice())
            .isFavorite(false)
            .build();
    }

    public AdvertisementResponse toResponse(Advertisement advertisement) {
        return AdvertisementResponse.builder()
            .id(advertisement.getId())
            .type(advertisement.getType().name())
            .typeDisplayName(advertisement.getType().getValue())
            .status(advertisement.getStatus().name())
            .authorId(advertisement.getAuthorId())
            .name(advertisement.getName())
            .description(advertisement.getDescription())
            .price(advertisement.getPrice())
            .photoUrls(advertisement.getPhotoUrls())
            .category(advertisement.getCategory() != null ?
                advertisement.getCategory().name() : null)
            .categoryDisplayName(advertisement.getCategory() != null ?
                advertisement.getCategory().getDisplayName() : null)
            .isFavorite(advertisement.isFavorite())
            .createdAt(advertisement.getCreatedAt())
            .build();
    }
}