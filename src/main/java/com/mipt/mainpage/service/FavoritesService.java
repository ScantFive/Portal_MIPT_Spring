package com.mipt.mainpage.service;

import com.mipt.mainpage.model.PageResponse;
import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.mainpage.repository.FavoritesRepository;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.repository.SearchRepository;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Сервис для работы со страницей избранных объявлений */
public class FavoritesService implements PageService {

  private static final long DEFAULT_PAGE_LIMIT = 50;
  private static final long DEFAULT_PAGE_OFFSET = 0;

  @Override
  public PageResponse getHomePage() {
    return getPage(null, null);
  }

  @Override
  public PageResponse getPage(SearchQuery search, UUID userId) {
    return getPage(search, userId, DEFAULT_PAGE_LIMIT, DEFAULT_PAGE_OFFSET);
  }

  /** Получить страницу избранных объявлений с указанными limit и offset */
  public PageResponse getPage(SearchQuery search, UUID userId, long limit, long offset) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null для страницы избранного");
    }

    SearchQuery effectiveSearch = Optional.ofNullable(search).orElseGet(SearchQuery::new);

    List<ShortAdvert> shortAdvertList =
        SearchRepository.getFavoriteAdverts(userId, limit, offset, effectiveSearch);

    long totalElements = FavoritesRepository.getFavoritesCount(userId);

    return PageResponse.builder()
        .userId(userId)
        .search(effectiveSearch)
        .shortAdverts(shortAdvertList)
        .totalElements(BigInteger.valueOf(totalElements))
        .build();
  }

  /**
   * Добавить объявление в избранное для пользователя
   *
   * @param userId ID пользователя
   * @param advertisementId ID объявления
   * @throws IllegalArgumentException если userId или advertisementId равны null
   */
  public void addToFavorites(UUID userId, UUID advertisementId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    if (advertisementId == null) {
      throw new IllegalArgumentException("Advertisement ID не может быть null");
    }
    FavoritesRepository.addToFavorites(userId, advertisementId);
  }

  /**
   * Удалить объявление из избранного для пользователя
   *
   * @param userId ID пользователя
   * @param advertisementId ID объявления
   * @throws IllegalArgumentException если userId или advertisementId равны null
   */
  public void removeFromFavorites(UUID userId, UUID advertisementId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    if (advertisementId == null) {
      throw new IllegalArgumentException("Advertisement ID не может быть null");
    }
    FavoritesRepository.removeFromFavorites(userId, advertisementId);
  }

  /**
   * Проверить, находится ли объявление в избранном у пользователя
   *
   * @param userId ID пользователя
   * @param advertisementId ID объявления
   * @return true если объявление в избранном, false в противном случае
   * @throws IllegalArgumentException если userId или advertisementId равны null
   */
  public boolean isFavorite(UUID userId, UUID advertisementId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    if (advertisementId == null) {
      throw new IllegalArgumentException("Advertisement ID не может быть null");
    }
    return FavoritesRepository.isFavorite(userId, advertisementId);
  }

  /**
   * Переключить статус избранного для объявления Если объявление уже в избранном - удаляет, иначе -
   * добавляет
   *
   * @param userId ID пользователя
   * @param advertisementId ID объявления
   * @return true если объявление было добавлено, false если было удалено
   */
  public boolean toggleFavorite(UUID userId, UUID advertisementId) {
    if (isFavorite(userId, advertisementId)) {
      removeFromFavorites(userId, advertisementId);
      return false;
    } else {
      addToFavorites(userId, advertisementId);
      return true;
    }
  }

  /**
   * Получить количество избранных объявлений пользователя
   *
   * @param userId ID пользователя
   * @return количество избранных объявлений
   */
  public long getFavoritesCount(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    return FavoritesRepository.getFavoritesCount(userId);
  }

  /**
   * Получить количество пользователей, добавивших объявление в избранное
   *
   * @param advertisementId ID объявления
   * @return количество пользователей
   */
  public long getFavoriteUsersCount(UUID advertisementId) {
    if (advertisementId == null) {
      throw new IllegalArgumentException("Advertisement ID не может быть null");
    }
    return FavoritesRepository.getFavoriteUsersCount(advertisementId);
  }
}
