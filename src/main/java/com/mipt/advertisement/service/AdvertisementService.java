package com.mipt.advertisement.service;

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
    return repository.save(advertisement);
  }

  @Override
  public Advertisement publish(Advertisement advertisement) {
    advertisement.validateToPublish();
    advertisement.setStatus(AdvertisementStatus.ACTIVE);
    return repository.save(advertisement);
  }

  @Override
  public Advertisement pause(Advertisement advertisement) {
    if (advertisement.getStatus() != AdvertisementStatus.ACTIVE) {
      throw new IllegalArgumentException(
          "Can only pause active advertisements. Current status: " + advertisement.getStatus());
    }
    advertisement.setStatus(AdvertisementStatus.PAUSED);
    return repository.save(advertisement);
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
    return repository.save(advertisement);
  }

  @Override
  public void delete(UUID id) {
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
    return repository.save(advertisement);
  }

  @Override
  public Advertisement removePhotoUrl(UUID advertisementId, String photoUrl) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    if (advertisement.getPhotoUrls() != null) {
      advertisement.getPhotoUrls().remove(photoUrl);
    }
    return repository.save(advertisement);
  }

  @Override
  public Advertisement updatePrice(UUID advertisementId, Long price) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    advertisement.setPrice(price);
    return repository.save(advertisement);
  }

  @Override
  public Advertisement toggleFavorite(UUID advertisementId) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    advertisement.setFavorite(!advertisement.isFavorite());
    return repository.save(advertisement);
  }

  @Override
  public Advertisement setFavorite(UUID advertisementId, boolean isFavorite) {
    Advertisement advertisement = repository.findById(advertisementId)
        .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
    advertisement.setFavorite(isFavorite);
    return repository.save(advertisement);
  }

  @Override
  public void clear() {
    repository.deleteAll();
  }
}
