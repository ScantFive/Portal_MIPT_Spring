package com.mipt.advertisement.service;

import com.mipt.advertisement.controller.dto.AdvertisementRequest;
import com.mipt.advertisement.controller.dto.AdvertisementResponse;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.model.Type;
import java.util.List;
import java.util.UUID;

public interface AdvertisementService {

    AdvertisementResponse createAdvertisement(AdvertisementRequest request);

    AdvertisementResponse getAdvertisement(UUID id);

    List<AdvertisementResponse> getAllAdvertisements();

    List<AdvertisementResponse> getAdvertisementsByAuthor(UUID authorId);

    List<AdvertisementResponse> getAdvertisementsByStatus(AdvertisementStatus status);

    List<AdvertisementResponse> getAdvertisementsByCategory(String categoryName);

    List<AdvertisementResponse> getAdvertisementsByType(Type type);

    List<AdvertisementResponse> getFavoriteAdvertisements();

    AdvertisementResponse updateAdvertisement(UUID id, AdvertisementRequest request);

    AdvertisementResponse publishAdvertisement(UUID id);

    AdvertisementResponse pauseAdvertisement(UUID id);

    void deleteAdvertisement(UUID id);

    AdvertisementResponse addPhoto(UUID id, String photoUrl);

    AdvertisementResponse removePhoto(UUID id, String photoUrl);

    AdvertisementResponse updatePrice(UUID id, Long price);

    AdvertisementResponse setFavorite(UUID id, boolean favorite);

    AdvertisementResponse toggleFavorite(UUID id);

    List<Category> getAllCategories();

    List<String> getCategoryGroups(Type type);
}