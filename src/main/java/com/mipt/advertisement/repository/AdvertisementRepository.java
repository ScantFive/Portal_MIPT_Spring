package com.mipt.advertisement.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.util.SpringContext;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class AdvertisementRepository implements AdvertisementRep {

  private AdvertisementJpaRepository repository() {
    return SpringContext.getBean(AdvertisementJpaRepository.class);
  }

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
      advertisement.setPhotoUrls(new TreeSet<>());
    }
    return repository().save(advertisement);
  }

  @Override
  public Advertisement publish(Advertisement advertisement) {
    advertisement.validateToPublish();
    advertisement.setStatus(AdvertisementStatus.ACTIVE);
    return repository().save(advertisement);
  }

  @Override
  public Advertisement pause(Advertisement advertisement) {
    if (advertisement.getStatus() != AdvertisementStatus.ACTIVE) {
      throw new IllegalArgumentException(
          "Can only pause active advertisements. Current status: " + advertisement.getStatus());
    }
    advertisement.setStatus(AdvertisementStatus.PAUSED);
    return repository().save(advertisement);
  }

  @Override
  public Optional<Advertisement> findById(UUID id) {
    return repository().findById(id);
  }

  @Override
  public List<Advertisement> findByAuthorId(UUID authorId) {
    return repository().findByAuthorId(authorId);
  }

  @Override
  public List<Advertisement> findByStatus(AdvertisementStatus status) {
    return repository().findByStatus(status);
  }

  @Override
  public List<Advertisement> findFavorites() {
    return repository().findByIsFavoriteTrue();
  }

  @Override
  public List<Advertisement> findByCategory(Category category) {
    return repository().findByCategory(category);
  }

  @Override
  public Category getCategory(UUID advertisementId) {
    return repository().findById(advertisementId).map(Advertisement::getCategory).orElse(null);
  }

  @Override
  public Advertisement setCategory(UUID advertisementId, Category category) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Advertisement not found: " + advertisementId));
    ad.setCategory(category);
    return repository().save(ad);
  }

  @Override
  public Set<Category> getAllCategories() {
    return new HashSet<>(repository().findAllCategories());
  }

  @Override
  public Advertisement update(Advertisement advertisement) {
    return repository().save(advertisement);
  }

  @Override
  public void delete(UUID id) throws SQLException {
    repository().deleteById(id);
  }

  @Override
  public Advertisement addPhotoUrl(UUID advertisementId, String photoUrl) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Advertisement not found: " + advertisementId));
    if (ad.getPhotoUrls() == null) {
      ad.setPhotoUrls(new TreeSet<>());
    }
    ad.getPhotoUrls().add(photoUrl);
    return repository().save(ad);
  }

  @Override
  public Advertisement removePhotoUrl(UUID advertisementId, String photoUrl) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Advertisement not found: " + advertisementId));
    if (ad.getPhotoUrls() != null) {
      ad.getPhotoUrls().remove(photoUrl);
    }
    return repository().save(ad);
  }

  @Override
  public Advertisement updatePrice(UUID advertisementId, Long price) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Advertisement not found: " + advertisementId));
    ad.setPrice(price);
    return repository().save(ad);
  }

  @Override
  public Advertisement toggleFavorite(UUID advertisementId) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Advertisement not found: " + advertisementId));
    ad.setFavorite(!ad.isFavorite());
    return repository().save(ad);
  }

  @Override
  public Advertisement setFavorite(UUID advertisementId, boolean isFavorite) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Advertisement not found: " + advertisementId));
    ad.setFavorite(isFavorite);
    return repository().save(ad);
  }

  @Override
  public void clear() {
    repository().deleteAll();
  }
}
