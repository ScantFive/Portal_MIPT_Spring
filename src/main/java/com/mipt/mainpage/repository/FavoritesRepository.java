package com.mipt.mainpage.repository;

import com.mipt.mainpage.model.Favorite;
import com.mipt.util.SpringContext;
import java.util.UUID;

/**
 * Репозиторий для управления избранными объявлениями. Использует таблицу
 * favorites для хранения
 * связей пользователь-объявление. Флаг is_favorite в advertisements обновляется
 * автоматически через
 * триггеры. Для поиска по избранным используйте
 * SearchRepository.getFavoriteAdverts()
 */
public class FavoritesRepository {

  private static FavoriteJpaRepository repository() {
    return SpringContext.getBean(FavoriteJpaRepository.class);
  }

  /** Проверить, является ли объявление избранным для конкретного пользователя */
  public static boolean isFavorite(UUID userId, UUID advertisementId) {
    return repository().existsByUserIdAndAdvertisementId(userId, advertisementId);
  }

  /**
   * Добавить объявление в избранное для пользователя Флаг is_favorite обновится
   * автоматически через
   * триггер
   */
  public static void addToFavorites(UUID userId, UUID advertisementId) {
    if (!isFavorite(userId, advertisementId)) {
      repository().save(Favorite.builder().userId(userId).advertisementId(advertisementId).build());
    }
  }

  /**
   * Удалить объявление из избранного для пользователя Флаг is_favorite обновится
   * автоматически
   * через триггер
   */
  public static void removeFromFavorites(UUID userId, UUID advertisementId) {
    repository().deleteByUserIdAndAdvertisementId(userId, advertisementId);
  }

  /** Получить количество избранных объявлений пользователя */
  public static long getFavoritesCount(UUID userId) {
    return repository().countByUserId(userId);
  }

  /** Получить количество пользователей, добавивших объявление в избранное */
  public static long getFavoriteUsersCount(UUID advertisementId) {
    return repository().countByAdvertisementId(advertisementId);
  }
}
