package com.mipt.service;

import com.mipt.controller.AdvertisementMapper;
import com.mipt.controller.dto.AdvertisementRequest;
import com.mipt.controller.dto.AdvertisementResponse;
import com.mipt.exception.AdvertisementNotFoundException;
import com.mipt.model.advertisement.Advertisement;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;
import com.mipt.model.advertisement.Type;
import com.mipt.repository.advertisement.AdvertisementRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementMapper advertisementMapper;

    @Override
    @Transactional
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request) {
        log.info("Creating new advertisement");

        Advertisement advertisement = advertisementMapper.toEntity(request);
        advertisement.validateToCreate();

        Advertisement saved = advertisementRepository.save(advertisement);
        log.info("Advertisement created with id: {}", saved.getId());

        return advertisementMapper.toResponse(saved);
    }

    @Override
    public AdvertisementResponse getAdvertisement(UUID id) {
        log.debug("Getting advertisement by id: {}", id);

        Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

        return advertisementMapper.toResponse(advertisement);
    }

    @Override
    public List<AdvertisementResponse> getAllAdvertisements() {
        log.debug("Getting all advertisements");

        return advertisementRepository.findAll().stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> getAdvertisementsByAuthor(UUID authorId) {
        log.debug("Getting advertisements by author: {}", authorId);

        return advertisementRepository.findByAuthorId(authorId).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> getAdvertisementsByStatus(AdvertisementStatus status) {
        log.debug("Getting advertisements by status: {}", status);

        return advertisementRepository.findByStatus(status).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> getAdvertisementsByCategory(String categoryName) {
        log.debug("Getting advertisements by category: {}", categoryName);

        Category category = Category.fromNameSafe(categoryName);
        if (category == null) {
            throw new IllegalArgumentException("Invalid category: " + categoryName);
        }

        return advertisementRepository.findByCategory(category).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> getAdvertisementsByType(Type type) {
        log.debug("Getting advertisements by type: {}", type);

        return advertisementRepository.findByType(type).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> getFavoriteAdvertisements() {
        log.debug("Getting favorite advertisements");

        return advertisementRepository.findFavorites().stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdvertisementResponse updateAdvertisement(UUID id, AdvertisementRequest request) {
        log.info("Updating advertisement: {}", id);

        Advertisement existing = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

        // Обновляем поля
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());

        Category newCategory = Category.fromNameSafe(request.getCategory());
        if (newCategory == null) {
            throw new IllegalArgumentException("Invalid category: " + request.getCategory());
        }
        existing.setCategory(newCategory);

        if (request.getPhotoUrls() != null) {
            existing.setPhotoUrls(request.getPhotoUrls());
        }

        existing.validateToCreate();

        Advertisement updated = advertisementRepository.update(existing);
        log.info("Advertisement updated: {}", id);

        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AdvertisementResponse publishAdvertisement(UUID id) {
        log.info("Publishing advertisement: {}", id);

        Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

        advertisement.validateToPublish();
        advertisement.setStatus(AdvertisementStatus.ACTIVE);

        Advertisement updated = advertisementRepository.update(advertisement);

        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AdvertisementResponse pauseAdvertisement(UUID id) {
        log.info("Pausing advertisement: {}", id);

        Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

        if (advertisement.getStatus() != AdvertisementStatus.ACTIVE) {
            throw new IllegalStateException(
                "Can only pause active advertisements. Current status: " + advertisement.getStatus()
            );
        }

        advertisement.setStatus(AdvertisementStatus.PAUSED);
        Advertisement updated = advertisementRepository.update(advertisement);

        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAdvertisement(UUID id) {
        log.info("Deleting advertisement: {}", id);

        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException(id);
        }

        advertisementRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AdvertisementResponse addPhoto(UUID id, String photoUrl) {
        log.debug("Adding photo to advertisement: {}", id);

        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException(id);
        }

        Advertisement updated = advertisementRepository.addPhoto(id, photoUrl);
        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AdvertisementResponse removePhoto(UUID id, String photoUrl) {
        log.debug("Removing photo from advertisement: {}", id);

        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException(id);
        }

        Advertisement updated = advertisementRepository.removePhoto(id, photoUrl);
        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AdvertisementResponse updatePrice(UUID id, Long price) {
        log.debug("Updating price for advertisement: {}", id);

        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException(id);
        }

        if (price != null && price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        Advertisement updated = advertisementRepository.updatePrice(id, price);
        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AdvertisementResponse setFavorite(UUID id, boolean favorite) {
        log.debug("Setting favorite={} for advertisement: {}", favorite, id);

        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException(id);
        }

        Advertisement updated = advertisementRepository.setFavorite(id, favorite);
        return advertisementMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AdvertisementResponse toggleFavorite(UUID id) {
        log.debug("Toggling favorite for advertisement: {}", id);

        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException(id);
        }

        Advertisement updated = advertisementRepository.toggleFavorite(id);
        return advertisementMapper.toResponse(updated);
    }

    @Override
    public List<Category> getAllCategories() {
        return advertisementRepository.getAllCategories();
    }

    @Override
    public List<String> getCategoryGroups(Type type) {
        return advertisementRepository.getAllCategoryGroups(type);
    }
}