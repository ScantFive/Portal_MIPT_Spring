package com.mipt.advertisement.controller;

import com.mipt.advertisement.controller.dto.AdvertisementRequest;
import com.mipt.advertisement.controller.dto.AdvertisementResponse;
import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
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

        Advertisement advertisement = Advertisement.builder()
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

        return advertisement;
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