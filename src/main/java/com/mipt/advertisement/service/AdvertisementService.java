package com.mipt.advertisement.service;

import com.mipt.advertisement.controller.dto.AdvertisementPatchRequest;
import com.mipt.advertisement.controller.dto.AdvertisementRequest;
import com.mipt.advertisement.controller.dto.AdvertisementResponse;
import com.mipt.advertisement.mapper.AdvertisementMapper;
import com.mipt.advertisement.event.AdvertisementEvent;
import com.mipt.advertisement.exception.AdvertisementNotFoundException;
import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.model.Type;
import com.mipt.advertisement.repository.AdvertisementRepository;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementService {

  private final AdvertisementRepository advertisementRepository;
  private final AdvertisementMapper advertisementMapper;
  private final KafkaEventPublisher eventPublisher;

  @Transactional
  public AdvertisementResponse createAdvertisement(AdvertisementRequest request) {
    log.info("Creating new advertisement");

    Advertisement advertisement = advertisementMapper.toEntity(request);
    advertisement.validateToCreate();

    Advertisement saved = advertisementRepository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.created(saved));
    log.info("Advertisement created with id: {}", saved.getId());
    return advertisementMapper.toResponse(saved);
  }

  public AdvertisementResponse getAdvertisement(UUID id) {
    log.debug("Getting advertisement by id: {}", id);

    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    return advertisementMapper.toResponse(advertisement);
  }

  @Transactional(readOnly = true)  // ← Добавить эту аннотацию
  public List<AdvertisementResponse> getAllAdvertisements() {
    log.debug("Getting all advertisements");

    List<Advertisement> advertisements = advertisementRepository.findAllByOrderByCreatedAtDesc();

    // Инициализируем фото внутри транзакции
    advertisements.forEach(ad -> ad.getPhotos().size()); // Принудительно загружаем фото

    return advertisements.stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
  }

  public List<AdvertisementResponse> getAdvertisementsByAuthor(UUID authorId) {
    log.debug("Getting advertisements by author: {}", authorId);

    return advertisementRepository.findByAuthorId(authorId).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
  }

  public List<AdvertisementResponse> getAdvertisementsByStatus(AdvertisementStatus status) {
    log.debug("Getting advertisements by status: {}", status);

    return advertisementRepository.findByStatus(status).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
  }

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

  public List<AdvertisementResponse> getAdvertisementsByType(Type type) {
    log.debug("Getting advertisements by type: {}", type);

    return advertisementRepository.findByType(type).stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
  }

  public List<AdvertisementResponse> getFavoriteAdvertisements() {
    log.debug("Getting favorite advertisements");

    return advertisementRepository.findByIsFavoriteTrue().stream()
            .map(advertisementMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public AdvertisementResponse updateAdvertisement(UUID id, AdvertisementRequest request) {
    log.info("Updating advertisement: {}", id);

    Advertisement existing = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

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

    Advertisement updated = advertisementRepository.save(existing);
    eventPublisher.publishEvent(AdvertisementEvent.updated(updated));
    log.info("Advertisement updated: {}", id);

    return advertisementMapper.toResponse(updated);
  }
  @Transactional
  public AdvertisementResponse patchAdvertisement(UUID id, AdvertisementPatchRequest patchRequest) {
    log.info("Partially updating advertisement: {}", id);

    Advertisement existing = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    // Применяем только те поля, которые пришли в запросе
    advertisementMapper.patchEntity(existing, patchRequest);

    // Валидация (опционально - только если обновили обязательные поля)
    if (patchRequest.getName() != null || patchRequest.getCategory() != null) {
      existing.validateToCreate();
    }

    Advertisement updated = advertisementRepository.save(existing);
    log.info("Advertisement partially updated: {}", id);

    return advertisementMapper.toResponse(updated);
  }

  @Transactional
  public AdvertisementResponse publishAdvertisement(UUID id) {
    log.info("Publishing advertisement: {}", id);

    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    advertisement.validateToPublish();
    advertisement.setStatus(AdvertisementStatus.ACTIVE);

    Advertisement updated = advertisementRepository.save(advertisement);

    eventPublisher.publishEvent(AdvertisementEvent.published(updated));

    return advertisementMapper.toResponse(updated);
  }

  @Transactional
  public AdvertisementResponse pauseAdvertisement(UUID id) {
    log.info("Pausing advertisement: {}", id);

    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    if (advertisement.getStatus() != AdvertisementStatus.ACTIVE) {
      throw new IllegalStateException(
              "Can only pause active advertisements. Current status: "
                      + advertisement.getStatus()
      );
    }

    advertisement.setStatus(AdvertisementStatus.PAUSED);
    Advertisement updated = advertisementRepository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.paused(updated));
    return advertisementMapper.toResponse(updated);
  }

  @Transactional
  public void deleteAdvertisement(UUID id) {
    log.info("Deleting advertisement: {}", id);

    if (!advertisementRepository.existsById(id)) {
      throw new AdvertisementNotFoundException(id);
    }
    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));
    eventPublisher.publishEvent(AdvertisementEvent.deleted(advertisement));
    advertisementRepository.deleteById(id);
  }

  @Transactional
  public AdvertisementResponse addPhoto(UUID id, String photoUrl) {
    log.debug("Adding photo to advertisement: {}", id);

    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    advertisement.addPhoto(photoUrl);
    Advertisement updated = advertisementRepository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.photoAdded(
            id, advertisement.getName(), advertisement.getAuthorId(), photoUrl));
    return advertisementMapper.toResponse(updated);
  }

  @Transactional
  public AdvertisementResponse removePhoto(UUID id, String photoUrl) {
    log.debug("Removing photo from advertisement: {}", id);

    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    advertisement.removePhoto(photoUrl);
    Advertisement updated = advertisementRepository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.photoRemoved(
            id, advertisement.getName(), advertisement.getAuthorId(), photoUrl));
    return advertisementMapper.toResponse(updated);
  }

  @Transactional
  public AdvertisementResponse updatePrice(UUID id, Long price) {
    log.debug("Updating price for advertisement: {}", id);
    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));
    Long oldPrice = advertisement.getPrice();
    if (!advertisementRepository.existsById(id)) {
      throw new AdvertisementNotFoundException(id);
    }

    if (price != null && price <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }

    int updated = advertisementRepository.updatePrice(id, price);
    if (updated == 0) {
      throw new RuntimeException("Failed to update price");
    }
    eventPublisher.publishEvent(AdvertisementEvent.priceChanged(
            id, advertisement.getName(), advertisement.getAuthorId(), oldPrice, price));
    return getAdvertisement(id);
  }

  @Transactional
  public AdvertisementResponse setFavorite(UUID id, boolean favorite) {
    log.debug("Setting favorite={} for advertisement: {}", favorite, id);

    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    advertisement.setFavorite(favorite);
    Advertisement updated = advertisementRepository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.favoriteToggled(
            id, advertisement.getName(), advertisement.getAuthorId(), favorite));
    return advertisementMapper.toResponse(updated);
  }

  @Transactional
  public AdvertisementResponse toggleFavorite(UUID id) {
    log.debug("Toggling favorite for advertisement: {}", id);
    Advertisement advertisement = advertisementRepository.findById(id)
            .orElseThrow(() -> new AdvertisementNotFoundException(id));

    int updated = advertisementRepository.toggleFavorite(id);
    if (updated == 0) {
      throw new AdvertisementNotFoundException(id);
    }
    eventPublisher.publishEvent(AdvertisementEvent.favoriteToggled(
            id, advertisement.getName(), advertisement.getAuthorId(),
            advertisement.isFavorite()));
    return getAdvertisement(id);
  }

  public List<Category> getAllCategories() {
    return Arrays.asList(Category.values());
  }

  public List<String> getCategoryGroups(Type type) {
    return Category.getGroupsForType(type);
  }
}