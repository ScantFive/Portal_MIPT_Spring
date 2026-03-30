package com.mipt.search.repository;

import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SearchQuery;
import com.mipt.util.SpringContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/** Репозиторий для работы с историей поисковых запросов. */
public class SearchHistoryRepository {

  private static SearchHistoryJpaRepository repository() {
    return SpringContext.getBean(SearchHistoryJpaRepository.class);
  }

  /** Сохраняет поисковый запрос в историю */
  public static void saveSearchHistory(UUID userId, SearchQuery query, int resultsCount) {
    if (userId == null) {
      return; // Не сохраняем историю для неавторизованных пользователей
    }
    try {
      SearchHistory history = SearchHistory.fromSearchQuery(userId, query, resultsCount);
      history.setId(UUID.randomUUID());
      history.setCreatedAt(LocalDateTime.now());
      repository().save(history);
    } catch (Exception e) {
      // Логируем ошибку, но не прерываем выполнение основного запроса
      System.err.println("Ошибка сохранения истории поиска: " + e.getMessage());
    }
  }

  /** Получает историю поисковых запросов пользователя */
  public static List<SearchHistory> getUserSearchHistory(UUID userId, int limit) {
    return repository().findByUserIdOrderByCreatedAtDesc(userId).stream().limit(limit).toList();
  }

  /** Получает последние уникальные поисковые запросы пользователя (по тексту) */
  public static List<String> getRecentSearchTexts(UUID userId, int limit) {
    return repository().findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(SearchHistory::getSearchText)
        .filter(text -> text != null && !text.isBlank())
        .map(text -> text.toLowerCase(Locale.ROOT).trim())
        .distinct()
        .limit(limit)
        .collect(Collectors.toList());
  }

  /** Получает популярные поисковые запросы пользователя */
  public static List<SearchHistory> getPopularSearches(UUID userId, int limit) {
    return repository().findByUserIdOrderByCreatedAtDesc(userId).stream()
        .collect(Collectors.groupingBy(SearchHistory::getSearchText))
        .entrySet().stream()
        .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
        .limit(limit)
        .map(entry -> entry.getValue().get(0))
        .collect(Collectors.toList());
  }

  /** Удаляет историю поиска пользователя */
  public static void clearUserSearchHistory(UUID userId) {
    repository().deleteByUserId(userId);
  }

  /** Удаляет конкретную запись из истории */
  public static void deleteSearchHistoryEntry(UUID historyId, UUID userId) {
    repository().findById(historyId).ifPresent(item -> {
      if (item.getUserId() != null && item.getUserId().equals(userId)) {
        repository().delete(item);
      }
    });
  }
}
