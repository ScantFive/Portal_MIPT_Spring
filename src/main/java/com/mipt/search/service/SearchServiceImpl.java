package com.mipt.search.service;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.*;
import com.mipt.search.repository.SearchHistoryRepository;
import com.mipt.search.repository.SearchRepository;
import com.mipt.search.repository.SearchSuggestionRepository;
import java.util.*;
import java.util.stream.Collectors;

/** Реализация сервиса поиска объявлений. */
public class SearchServiceImpl implements SearchService {

  @Override
  public List<ShortAdvert> search(long limit, long offset, SearchQuery query) {
    return search(limit, offset, query, null);
  }

  @Override
  public List<ShortAdvert> search(long limit, long offset, SearchQuery query, UUID userId) {
    SearchQuery effectiveQuery = Optional.ofNullable(query).orElseGet(SearchQuery::new);
    List<ShortAdvert> results = SearchRepository.getAdverts(limit, offset, effectiveQuery, userId);

    // Сохраняем запрос в историю, если пользователь авторизован и запрос не пустой
    if (userId != null && isSignificantQuery(effectiveQuery)) {
      SearchHistoryRepository.saveSearchHistory(userId, effectiveQuery, results.size());
    }

    return results;
  }

  /** Проверяет, является ли запрос значимым для сохранения в историю */
  private boolean isSignificantQuery(SearchQuery query) {
    return (query.getSearchText() != null && !query.getSearchText().trim().isEmpty())
        || query.getType() != null
        || (query.getCategory() != null && !query.getCategory().isEmpty())
        || (query.getFilters() != null && !query.getFilters().isEmpty());
  }

  @Override
  public List<ShortAdvert> searchByText(String searchText, long limit, long offset) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(searchText);
    return search(limit, offset, query);
  }

  @Override
  public List<ShortAdvert> searchByType(SearchType type, long limit, long offset) {
    SearchQuery query = new SearchQuery();
    query.setType(type);
    return search(limit, offset, query);
  }

  @Override
  public List<ShortAdvert> searchByCategory(String categoryTitle, long limit, long offset) {
    SearchQuery query = new SearchQuery();
    SearchCategory category = new SearchCategory();
    category.setCategoryTitle(categoryTitle);
    category.setActive(true);
    query.setCategory(Collections.singletonList(category));
    return search(limit, offset, query);
  }

  @Override
  public List<ShortAdvert> searchFavorites(
      UUID userId, SearchQuery query, long limit, long offset) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null для поиска в избранном");
    }

    SearchQuery effectiveQuery = Optional.ofNullable(query).orElseGet(SearchQuery::new);

    List<ShortAdvert> results =
        SearchRepository.getFavoriteAdverts(userId, limit, offset, effectiveQuery);

    // Сохраняем поиск в избранном в историю
    if (isSignificantQuery(effectiveQuery)) {
      SearchHistoryRepository.saveSearchHistory(userId, effectiveQuery, results.size());
    }

    return results;
  }

  // Методы работы с историей поиска

  @Override
  public List<SearchHistory> getUserSearchHistory(UUID userId, int limit) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    return SearchHistoryRepository.getUserSearchHistory(userId, limit);
  }

  @Override
  public List<String> getRecentSearchTexts(UUID userId, int limit) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    return SearchHistoryRepository.getRecentSearchTexts(userId, limit);
  }

  @Override
  public List<SearchHistory> getPopularSearches(UUID userId, int limit) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    return SearchHistoryRepository.getPopularSearches(userId, limit);
  }

  @Override
  public void clearUserSearchHistory(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null");
    }
    SearchHistoryRepository.clearUserSearchHistory(userId);
  }

  @Override
  public void deleteSearchHistoryEntry(UUID historyId, UUID userId) {
    if (historyId == null || userId == null) {
      throw new IllegalArgumentException("History ID и User ID не могут быть null");
    }
    SearchHistoryRepository.deleteSearchHistoryEntry(historyId, userId);
  }

  // Методы работы с контекстными подсказками

  @Override
  public List<SearchSuggestion> getSearchSuggestions(String prefix, UUID userId, int limit) {
    List<SearchSuggestion> allSuggestions = new ArrayList<>();

    // Определяем количество подсказок каждого типа
    int historyLimit = limit / 4;
    int autocompleteLimit = limit / 3;
    int popularLimit = limit / 4;
    int trendingLimit = limit / 4;

    // Собираем подсказки из разных источников
    if (userId != null) {
      // Для авторизованных пользователей добавляем персональные подсказки
      List<SearchSuggestion> historySuggestions =
          getHistorySuggestions(userId, prefix, historyLimit);
      allSuggestions.addAll(historySuggestions);

      // Добавляем персонализированные подсказки
      List<SearchSuggestion> personalizedSuggestions =
          getPersonalizedSuggestions(userId, prefix, Math.max(1, historyLimit / 2));
      allSuggestions.addAll(personalizedSuggestions);
    }

    // Автодополнение из объявлений (для всех пользователей)
    if (prefix != null && !prefix.trim().isEmpty()) {
      List<SearchSuggestion> autocompleteSuggestions =
          getAutocompleteSuggestions(prefix, autocompleteLimit);
      allSuggestions.addAll(autocompleteSuggestions);
    }

    // Популярные подсказки
    List<SearchSuggestion> popularSuggestions = getPopularSuggestions(prefix, popularLimit);
    allSuggestions.addAll(popularSuggestions);

    // Трендовые подсказки
    List<SearchSuggestion> trendingSuggestions = getTrendingSuggestions(prefix, trendingLimit);
    allSuggestions.addAll(trendingSuggestions);

    // Удаляем дубликаты, оставляя подсказку с наивысшей релевантностью
    Map<String, SearchSuggestion> uniqueSuggestions = new LinkedHashMap<>();
    for (SearchSuggestion suggestion : allSuggestions) {
      String key = suggestion.getText().toLowerCase().trim();
      if (!uniqueSuggestions.containsKey(key)
          || (suggestion.getRelevance() != null
              && (uniqueSuggestions.get(key).getRelevance() == null
                  || suggestion.getRelevance() > uniqueSuggestions.get(key).getRelevance()))) {
        uniqueSuggestions.put(key, suggestion);
      }
    }

    // Сортируем по релевантности и возвращаем топ результаты
    return uniqueSuggestions.values().stream()
        .sorted(
            (s1, s2) -> {
              Double r1 = s1.getRelevance() != null ? s1.getRelevance() : 0.0;
              Double r2 = s2.getRelevance() != null ? s2.getRelevance() : 0.0;
              return Double.compare(r2, r1); // Сортировка по убыванию
            })
        .limit(limit)
        .collect(Collectors.toList());
  }

  @Override
  public List<SearchSuggestion> getHistorySuggestions(UUID userId, String prefix, int limit) {
    if (userId == null) {
      return Collections.emptyList();
    }
    return SearchSuggestionRepository.getHistorySuggestions(userId, prefix, limit);
  }

  @Override
  public List<SearchSuggestion> getPopularSuggestions(String prefix, int limit) {
    return SearchSuggestionRepository.getPopularSuggestions(prefix, limit);
  }

  @Override
  public List<SearchSuggestion> getAutocompleteSuggestions(String prefix, int limit) {
    if (prefix == null || prefix.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return SearchSuggestionRepository.getAutocompleteSuggestions(prefix, limit);
  }

  @Override
  public List<SearchSuggestion> getTrendingSuggestions(String prefix, int limit) {
    // По умолчанию анализируем последние 7 дней
    return SearchSuggestionRepository.getTrendingSuggestions(prefix, 7, limit);
  }

  @Override
  public List<SearchSuggestion> getPersonalizedSuggestions(UUID userId, String prefix, int limit) {
    if (userId == null) {
      return Collections.emptyList();
    }
    return SearchSuggestionRepository.getPersonalizedSuggestions(userId, prefix, limit);
  }
}
