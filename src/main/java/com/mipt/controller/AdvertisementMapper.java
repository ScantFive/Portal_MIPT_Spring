package com.mipt.controller;

import com.mipt.controller.dto.AdvertisementRequest;
import com.mipt.controller.dto.AdvertisementResponse;
import com.mipt.model.advertisement.Advertisement;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;
import com.mipt.model.advertisement.Type;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class AdvertisementMapper {

    public Advertisement toEntity(AdvertisementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Безопасное преобразование категории
        Category category = Category.fromNameSafe(request.getCategory());
        if (category == null) {
            throw new IllegalArgumentException(
                    String.format("Некорректная категория: '%s'. Доступные категории: %s",
                            request.getCategory(),
                            Arrays.stream(Category.values())
                                    .map(Category::getDisplayName)
                                    .toList())
            );
        }

        // Проверка соответствия типа и категории
        Type categoryType = Category.getTypeForCategory(category);
        if (categoryType != request.getType()) {
            throw new IllegalArgumentException(
                    String.format("Категория '%s' (тип: %s) не соответствует типу объявления: %s",
                            category.getDisplayName(), categoryType, request.getType())
            );
        }

        Advertisement advertisement = Advertisement.builder()
                .type(request.getType())
                .status(AdvertisementStatus.DRAFT)
                .authorId(request.getAuthorId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .isFavorite(false)
                .createdAt(Instant.now())
                .build();

        // Устанавливаем фото
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            advertisement.setPhotoUrls(new LinkedHashSet<>(request.getPhotoUrls()));
        }

        return advertisement;
    }

    public AdvertisementResponse toResponse(Advertisement advertisement) {
        if (advertisement == null) {
            return null;
        }

        return AdvertisementResponse.builder()
                .id(advertisement.getId())
                .type(advertisement.getType() != null ? advertisement.getType().name() : null)
                .typeDisplayName(advertisement.getType() != null ? advertisement.getType().getValue() : null)
                .status(advertisement.getStatus() != null ? advertisement.getStatus().name() : null)
                .authorId(advertisement.getAuthorId())
                .name(advertisement.getName())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())
                .photoUrls(advertisement.getPhotoUrls() != null ?
                        advertisement.getPhotoUrls() : new LinkedHashSet<>())
                .category(advertisement.getCategoryName())
                .categoryDisplayName(advertisement.getCategoryDisplayName())
                .isFavorite(advertisement.isFavorite())
                .createdAt(advertisement.getCreatedAt())
                .build();
    }

    // Метод для обновления существующего объявления из запроса
    public void updateEntity(Advertisement existing, AdvertisementRequest request) {
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            Category category = Category.fromNameSafe(request.getCategory());
            if (category != null) {
                existing.setCategory(category);
            }
        }
        if (request.getPhotoUrls() != null) {
            existing.setPhotoUrls(new LinkedHashSet<>(request.getPhotoUrls()));
        }
    }
}