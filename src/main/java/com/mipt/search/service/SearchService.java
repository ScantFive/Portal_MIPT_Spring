package com.mipt.search.service;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SearchType;
import java.util.List;
import java.util.UUID;

public interface SearchService {

  List<ShortAdvert> search(long limit, long offset, SearchQuery query);

  List<ShortAdvert> search(long limit, long offset, SearchQuery query, UUID userId);

  List<ShortAdvert> searchByText(String searchText, long limit, long offset);

  List<ShortAdvert> searchByType(SearchType type, long limit, long offset);

  List<ShortAdvert> searchByCategory(String categoryTitle, long limit, long offset);

  List<ShortAdvert> searchFavorites(UUID userId, SearchQuery query, long limit, long offset);

  // Методы работы с историей поиска
  List<SearchHistory> getUserSearchHistory(UUID userId, int limit);

  List<String> getRecentSearchTexts(UUID userId, int limit);

  List<SearchHistory> getPopularSearches(UUID userId, int limit);

  void clearUserSearchHistory(UUID userId);

  void deleteSearchHistoryEntry(UUID historyId, UUID userId);

  // Методы работы с контекстными подсказками

  /**
   * Получает контекстные подсказки для поиска
   *
   * @param prefix Префикс для фильтрации подсказок
   * @param userId ID пользователя (может быть null для неавторизованных)
   * @param limit Максимальное количество подсказок
   * @return Список подсказок, отсортированный по релевантности
   */
  List<SearchSuggestion> getSearchSuggestions(String prefix, UUID userId, int limit);

  /**
   * Получает подсказки из истории пользователя
   *
   * @param userId ID пользователя
   * @param prefix Префикс для фильтрации
   * @param limit Максимальное количество подсказок
   * @return Список подсказок из истории
   */
  List<SearchSuggestion> getHistorySuggestions(UUID userId, String prefix, int limit);

  /**
   * Получает популярные подсказки
   *
   * @param prefix Префикс для фильтрации
   * @param limit Максимальное количество подсказок
   * @return Список популярных подсказок
   */
  List<SearchSuggestion> getPopularSuggestions(String prefix, int limit);

  /**
   * Получает подсказки автодополнения из объявлений
   *
   * @param prefix Префикс для поиска
   * @param limit Максимальное количество подсказок
   * @return Список подсказок автодополнения
   */
  List<SearchSuggestion> getAutocompleteSuggestions(String prefix, int limit);

  /**
   * Получает трендовые подсказки
   *
   * @param prefix Префикс для фильтрации
   * @param limit Максимальное количество подсказок
   * @return Список трендовых подсказок
   */
  List<SearchSuggestion> getTrendingSuggestions(String prefix, int limit);

  /**
   * Получает персонализированные подсказки
   *
   * @param userId ID пользователя
   * @param prefix Префикс для фильтрации
   * @param limit Максимальное количество подсказок
   * @return Список персонализированных подсказок
   */
  List<SearchSuggestion> getPersonalizedSuggestions(UUID userId, String prefix, int limit);
}
