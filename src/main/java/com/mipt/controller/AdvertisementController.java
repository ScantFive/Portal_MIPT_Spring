package com.mipt.controller;

import com.mipt.controller.dto.AdvertisementRequest;
import com.mipt.controller.dto.AdvertisementResponse;
import com.mipt.controller.dto.CategoryResponse;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;
import com.mipt.model.advertisement.Type;
import com.mipt.service.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @PostMapping
    public ResponseEntity<AdvertisementResponse> createAdvertisement(
        @Valid @RequestBody AdvertisementRequest request) {
        log.info("POST /api/v1/advertisements - creating new advertisement");
        AdvertisementResponse response = advertisementService.createAdvertisement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> getAdvertisement(@PathVariable UUID id) {
        log.info("GET /api/v1/advertisements/{}", id);
        AdvertisementResponse response = advertisementService.getAdvertisement(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementResponse>> getAllAdvertisements(
        @RequestParam(required = false) UUID authorId,
        @RequestParam(required = false) AdvertisementStatus status,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Type type,
        @RequestParam(required = false) Boolean favorite) {

        log.info("GET /api/v1/advertisements with filters - authorId: {}, status: {}, category: {}, type: {}, favorite: {}",
            authorId, status, category, type, favorite);

        List<AdvertisementResponse> responses;

        if (authorId != null) {
            responses = advertisementService.getAdvertisementsByAuthor(authorId);
        } else if (status != null) {
            responses = advertisementService.getAdvertisementsByStatus(status);
        } else if (category != null) {
            responses = advertisementService.getAdvertisementsByCategory(category);
        } else if (type != null) {
            responses = advertisementService.getAdvertisementsByType(type);
        } else if (favorite != null && favorite) {
            responses = advertisementService.getFavoriteAdvertisements();
        } else {
            responses = advertisementService.getAllAdvertisements();
        }

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> updateAdvertisement(
        @PathVariable UUID id,
        @Valid @RequestBody AdvertisementRequest request) {
        log.info("PUT /api/v1/advertisements/{}", id);
        AdvertisementResponse response = advertisementService.updateAdvertisement(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<AdvertisementResponse> publishAdvertisement(@PathVariable UUID id) {
        log.info("PATCH /api/v1/advertisements/{}/publish", id);
        AdvertisementResponse response = advertisementService.publishAdvertisement(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<AdvertisementResponse> pauseAdvertisement(@PathVariable UUID id) {
        log.info("PATCH /api/v1/advertisements/{}/pause", id);
        AdvertisementResponse response = advertisementService.pauseAdvertisement(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable UUID id) {
        log.info("DELETE /api/v1/advertisements/{}", id);
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<AdvertisementResponse> addPhoto(
        @PathVariable UUID id,
        @RequestParam String photoUrl) {
        log.info("POST /api/v1/advertisements/{}/photos - url: {}", id, photoUrl);
        AdvertisementResponse response = advertisementService.addPhoto(id, photoUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<AdvertisementResponse> removePhoto(
        @PathVariable UUID id,
        @RequestParam String photoUrl) {
        log.info("DELETE /api/v1/advertisements/{}/photos - url: {}", id, photoUrl);
        AdvertisementResponse response = advertisementService.removePhoto(id, photoUrl);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<AdvertisementResponse> updatePrice(
        @PathVariable UUID id,
        @RequestParam(required = false) Long price) {
        log.info("PATCH /api/v1/advertisements/{}/price - price: {}", id, price);
        AdvertisementResponse response = advertisementService.updatePrice(id, price);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<AdvertisementResponse> setFavorite(
        @PathVariable UUID id,
        @RequestParam boolean favorite) {
        log.info("PATCH /api/v1/advertisements/{}/favorite - favorite: {}", id, favorite);
        AdvertisementResponse response = advertisementService.setFavorite(id, favorite);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/favorite/toggle")
    public ResponseEntity<AdvertisementResponse> toggleFavorite(@PathVariable UUID id) {
        log.info("POST /api/v1/advertisements/{}/favorite/toggle", id);
        AdvertisementResponse response = advertisementService.toggleFavorite(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(
        @RequestParam(required = false) Type type) {
        log.info("GET /api/v1/advertisements/categories - type: {}", type);

        if (type != null) {
            List<String> groups = advertisementService.getCategoryGroups(type);
            List<CategoryResponse> responses = groups.stream()
                .map(group -> CategoryResponse.builder()
                    .groupName(group)
                    .categories(Category.getCategoriesInGroup(group).stream()
                        .map(cat -> CategoryResponse.CategoryInfo.builder()
                            .name(cat.name())
                            .displayName(cat.getDisplayName())
                            .type(Category.getTypeForCategory(cat).name())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } else {
            List<Category> allCategories = advertisementService.getAllCategories();
            List<CategoryResponse> responses = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getGroup))
                .entrySet().stream()
                .map(entry -> CategoryResponse.builder()
                    .groupName(entry.getKey())
                    .categories(entry.getValue().stream()
                        .map(cat -> CategoryResponse.CategoryInfo.builder()
                            .name(cat.name())
                            .displayName(cat.getDisplayName())
                            .type(Category.getTypeForCategory(cat).name())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }
    }
}