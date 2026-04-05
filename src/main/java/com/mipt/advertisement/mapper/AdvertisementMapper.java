package com.mipt.advertisement.mapper;

import com.mipt.advertisement.controller.dto.AdvertisementPatchRequest;
import com.mipt.advertisement.controller.dto.AdvertisementRequest;
import com.mipt.advertisement.controller.dto.AdvertisementResponse;
import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.model.Type;
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

    public void patchEntity(Advertisement existing, AdvertisementPatchRequest patch) {
        if (patch.getName() != null) {
            existing.setName(patch.getName());
        }

        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }

        if (patch.getPrice() != null) {
            existing.setPrice(patch.getPrice());
        }

        if (patch.getCategory() != null) {
            Category category = Category.fromNameSafe(patch.getCategory());
            if (category != null) {
                existing.setCategory(category);
            }
        }

        if (patch.getPhotoUrls() != null) {
            existing.setPhotoUrls(patch.getPhotoUrls());
        }
    }
}