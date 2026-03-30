package com.mipt.search.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.repository.AdvertisementJpaRepository;
import com.mipt.util.SpringContext;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/** Репозиторий для работы с фотографиями объявлений. */
public class AdvertisementPhotosRepository {

  private static AdvertisementJpaRepository repository() {
    return SpringContext.getBean(AdvertisementJpaRepository.class);
  }

  /**
   * Добавить фотографию к объявлению
   *
   * @param advertisementId ID объявления
   * @param photoUrl        URL фотографии
   * @param displayOrder    порядок отображения (необязательно)
   * @return ID добавленной фотографии
   */
  public static long addPhoto(UUID advertisementId, String photoUrl, Integer displayOrder) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Не найдено объявление: " + advertisementId));
    if (ad.getPhotoUrls() == null) {
      ad.setPhotoUrls(new LinkedHashSet<>());
    }
    ad.getPhotoUrls().add(photoUrl);
    repository().save(ad);
    return ad.getPhotoUrls().size();
  }

  /**
   * Добавить несколько фотографий к объявлению
   *
   * @param advertisementId ID объявления
   * @param photoUrls       список URL фотографий
   */
  public static void addPhotos(UUID advertisementId, List<String> photoUrls) {
    if (photoUrls == null || photoUrls.isEmpty()) {
      return;
    }

    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Не найдено объявление: " + advertisementId));
    if (ad.getPhotoUrls() == null) {
      ad.setPhotoUrls(new LinkedHashSet<>());
    }
    photoUrls.stream().filter(url -> url != null && !url.isBlank()).forEach(ad.getPhotoUrls()::add);
    repository().save(ad);
  }

  /**
   * Получить все фотографии объявления
   *
   * @param advertisementId ID объявления
   * @return список URL фотографий в порядке отображения
   */
  public static List<String> getPhotos(UUID advertisementId) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Не найдено объявление: " + advertisementId));
    return ad.getPhotoUrls() == null ? new ArrayList<>() : new ArrayList<>(ad.getPhotoUrls());
  }

  /**
   * Удалить фотографию по ID
   *
   * @param photoId ID фотографии
   * @return true, если фотография была удалена
   */
  public static boolean deletePhoto(long photoId) {
    return false;
  }

  /**
   * Удалить фотографию по URL
   *
   * @param advertisementId ID объявления
   * @param photoUrl        URL фотографии
   * @return true, если фотография была удалена
   */
  public static boolean deletePhotoByUrl(UUID advertisementId, String photoUrl) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Не найдено объявление: " + advertisementId));
    if (ad.getPhotoUrls() == null) {
      return false;
    }
    boolean removed = ad.getPhotoUrls().remove(photoUrl);
    if (removed) {
      repository().save(ad);
    }
    return removed;
  }

  /**
   * Удалить все фотографии объявления
   *
   * @param advertisementId ID объявления
   * @return количество удаленных фотографий
   */
  public static int deleteAllPhotos(UUID advertisementId) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Не найдено объявление: " + advertisementId));
    int count = ad.getPhotoUrls() == null ? 0 : ad.getPhotoUrls().size();
    ad.setPhotoUrls(new LinkedHashSet<>());
    repository().save(ad);
    return count;
  }

  /**
   * Обновить порядок отображения фотографии
   *
   * @param photoId  ID фотографии
   * @param newOrder новый порядок отображения
   * @return true, если порядок был обновлен
   */
  public static boolean updatePhotoOrder(long photoId, int newOrder) {
    return false;
  }

  /**
   * Получить количество фотографий у объявления
   *
   * @param advertisementId ID объявления
   * @return количество фотографий
   */
  public static int getPhotosCount(UUID advertisementId) {
    Advertisement ad = repository().findById(advertisementId)
        .orElseThrow(() -> new RuntimeException("Не найдено объявление: " + advertisementId));
    return ad.getPhotoUrls() == null ? 0 : ad.getPhotoUrls().size();
  }
}
