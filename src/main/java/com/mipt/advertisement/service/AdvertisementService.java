package com.mipt.advertisement.service;

import com.mipt.advertisement.event.AdvertisementEvent;
import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.repository.AdvertisementJpaRepository;
import com.mipt.advertisement.repository.AdvertisementRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AdvertisementService implements AdvertisementRep {

  private final AdvertisementJpaRepository repository;
  private final KafkaEventPublisher eventPublisher;

  @Override
  public Advertisement create(Advertisement advertisement) {
    advertisement.validateToCreate();
    if (advertisement.getId() == null) {
      advertisement.setId(UUID.randomUUID());
    }
    if (advertisement.getCreatedAt() == null) {
      advertisement.setCreatedAt(Instant.now());
    }
    if (advertisement.getStatus() == null) {
      advertisement.setStatus(AdvertisementStatus.DRAFT);
    }
    if (advertisement.getPhotoUrls() == null) {
      advertisement.setPhotoUrls(new HashSet<>());
    }
    Advertisement saved = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.created(saved));
    return saved;
  }

  @Override
  public Advertisement publish(Advertisement advertisement) {
    advertisement.validateToPublish();
    advertisement.setStatus(AdvertisementStatus.ACTIVE);
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.published(updated));
    return updated;
  }

  @Override
  public Advertisement pause(Advertisement advertisement) {
    if (advertisement.getStatus() != AdvertisementStatus.ACTIVE) {
      throw new IllegalArgumentException(
          "Can only pause active advertisements. Current status: " + advertisement.getStatus());
    }
    advertisement.setStatus(AdvertisementStatus.PAUSED);
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.paused(updated));
    return updated;
  }

  @Override
  public Optional<Advertisement> findById(UUID id) {
    return repository.findById(id);
  }

  @Override
  public List<Advertisement> findByAuthorId(UUID authorId) {
    return repository.findByAuthorId(authorId);
  }

  @Override
  public List<Advertisement> findByStatus(AdvertisementStatus status) {
    return repository.findByStatus(status);
  }

  @Override
  public List<Advertisement> findFavorites() {
    return repository.findByIsFavoriteTrue();
  }

  @Override
  public List<Advertisement> findByCategory(Category category) {
    return repository.findByCategory(category);
  }

  @Override
  public Category getCategory(UUID advertisementId) {
    return repository.findById(advertisementId)
        .map(Advertisement::getCategory)
        .orElse(null);
  }

  @Override
  public Advertisement setCategory(UUID advertisementId, Category category) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    advertisement.setCategory(category);
    return repository.save(advertisement);
  }

  @Override
  public Set<Category> getAllCategories() {
    List<Category> categories = repository.findAllCategories();
    return new HashSet<>(categories);
  }

  @Override
  public Advertisement update(Advertisement advertisement) {
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.updated(updated));
    return updated;
  }

  @Override
  public void delete(UUID id) {
    Advertisement advertisement = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + id));
    eventPublisher.publishEvent(AdvertisementEvent.deleted(advertisement));
    repository.deleteById(id);
  }

  @Override
  public Advertisement addPhotoUrl(UUID advertisementId, String photoUrl) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    if (advertisement.getPhotoUrls() == null) {
      advertisement.setPhotoUrls(new HashSet<>());
    }
    advertisement.getPhotoUrls().add(photoUrl);
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.photoAdded(
        advertisement.getId(),
        advertisement.getName(),
        advertisement.getAuthorId(),
        photoUrl));
    return updated;
  }

  @Override
  public Advertisement removePhotoUrl(UUID advertisementId, String photoUrl) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    if (advertisement.getPhotoUrls() != null) {
      advertisement.getPhotoUrls().remove(photoUrl);
    }
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.photoRemoved(
        advertisement.getId(),
        advertisement.getName(),
        advertisement.getAuthorId(),
        photoUrl));
    return updated;
  }

  @Override
  public Advertisement updatePrice(UUID advertisementId, Long price) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    Long oldPrice = advertisement.getPrice();
    advertisement.setPrice(price);
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.priceChanged(
        advertisement.getId(),
        advertisement.getName(),
        advertisement.getAuthorId(),
        oldPrice,
        price));
    return updated;
  }

  @Override
  public Advertisement toggleFavorite(UUID advertisementId) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    advertisement.setFavorite(!advertisement.isFavorite());
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.favoriteToggled(
        advertisement.getId(),
        advertisement.getName(),
        advertisement.getAuthorId(),
        updated.isFavorite()));
    return updated;
  }

  @Override
  public Advertisement setFavorite(UUID advertisementId, boolean isFavorite) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    advertisement.setFavorite(isFavorite);
    Advertisement updated = repository.save(advertisement);
    eventPublisher.publishEvent(AdvertisementEvent.favoriteToggled(
        advertisement.getId(),
        advertisement.getName(),
        advertisement.getAuthorId(),
        isFavorite));
    return updated;
  }

  @Override
  public void clear() {
    repository.deleteAll();
  }
}
