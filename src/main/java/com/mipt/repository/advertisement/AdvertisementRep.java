package com.mipt.repository.advertisement;

import com.mipt.model.advertisement.Advertisement;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AdvertisementRep {

  Advertisement create(Advertisement advertisement);

  Advertisement publish(Advertisement advertisement);

  Advertisement pause(Advertisement advertisement);

  Optional<Advertisement> findById(UUID id);

  List<Advertisement> findByAuthorId(UUID authorId);

  List<Advertisement> findByStatus(AdvertisementStatus status);

  List<Advertisement> findFavorites();

  List<Advertisement> findByCategory(Category category);

  // Методы для работы с одной категорией
  Category getCategory(UUID advertisementId);

  Advertisement setCategory(UUID advertisementId, Category category);

  Set<Category> getAllCategories();

  Advertisement update(Advertisement advertisement);

  void delete(UUID id) throws SQLException;

  Advertisement addPhotoUrl(UUID advertisementId, String photoUrl);

  Advertisement removePhotoUrl(UUID advertisementId, String photoUrl);

  Advertisement updatePrice(UUID advertisementId, Long price);

  Advertisement toggleFavorite(UUID advertisementId);

  Advertisement setFavorite(UUID advertisementId, boolean isFavorite);

  void clear();
}