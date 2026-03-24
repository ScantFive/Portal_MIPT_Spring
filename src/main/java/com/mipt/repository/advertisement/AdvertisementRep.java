package com.mipt.repository.advertisement;

import com.mipt.model.advertisement.Advertisement;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;
import com.mipt.model.advertisement.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRep {

    Advertisement save(Advertisement advertisement);

    Optional<Advertisement> findById(UUID id);

    List<Advertisement> findAll();

    List<Advertisement> findByAuthorId(UUID authorId);

    List<Advertisement> findByStatus(AdvertisementStatus status);

    List<Advertisement> findByCategory(Category category);

    List<Advertisement> findByType(Type type);

    List<Advertisement> findFavorites();

    Advertisement update(Advertisement advertisement);

    void deleteById(UUID id);

    Advertisement addPhoto(UUID advertisementId, String photoUrl);

    Advertisement removePhoto(UUID advertisementId, String photoUrl);

    Advertisement updatePrice(UUID advertisementId, Long price);

    Advertisement setFavorite(UUID advertisementId, boolean favorite);

    Advertisement toggleFavorite(UUID advertisementId);

    List<Category> getAllCategories();

    List<String> getAllCategoryGroups(Type type);

    boolean existsById(UUID id);
}