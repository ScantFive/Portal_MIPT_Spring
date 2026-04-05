package com.mipt.search.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.repository.AdvertisementRepository;
import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SuggestionType;
import com.mipt.util.SpringContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Репозиторий для работы с контекстными подсказками поиска. */
public class SearchSuggestionRepository {

  private static SearchHistoryJpaRepository historyRepository() {
    return SpringContext.getBean(SearchHistoryJpaRepository.class);
  }

  private static AdvertisementRepository adRepository() {
    return SpringContext.getBean(AdvertisementRepository.class);
  }

  /**
   * Получает подсказки из истории поиска пользователя
   *
   * @param userId ID пользователя
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit  Максимальное количество подсказок
   * @return Список подсказок из истории
   */
  public static List<SearchSuggestion> getHistorySuggestions(
      UUID userId, String prefix, int limit) {
    String effectivePrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
    return historyRepository().findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(SearchHistory::getSearchText)
        .filter(text -> text != null && !text.isBlank())
        .filter(text -> effectivePrefix.isBlank() || text.toLowerCase(Locale.ROOT).startsWith(effectivePrefix))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(limit)
        .map(e -> new SearchSuggestion(e.getKey(), SuggestionType.HISTORY, e.getValue().doubleValue()))
        .collect(Collectors.toList());
  }

  /**
   * Получает популярные подсказки на основе частоты использования
   *
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit  Максимальное количество подсказок
   * @return Список популярных подсказок
   */
  public static List<SearchSuggestion> getPopularSuggestions(String prefix, int limit) {
    String effectivePrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
    return historyRepository().findAll().stream()
        .map(SearchHistory::getSearchText)
        .filter(text -> text != null && !text.isBlank())
        .filter(text -> effectivePrefix.isBlank() || text.toLowerCase(Locale.ROOT).startsWith(effectivePrefix))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(limit)
        .map(e -> new SearchSuggestion(e.getKey(), SuggestionType.POPULAR, e.getValue().doubleValue()))
        .collect(Collectors.toList());
  }

  /**
   * Получает подсказки автодополнения из названий объявлений
   *
   * @param prefix Префикс для поиска
   * @param limit  Максимальное количество подсказок
   * @return Список подсказок автодополнения
   */
  public static List<SearchSuggestion> getAutocompleteSuggestions(String prefix, int limit) {
    if (prefix == null || prefix.trim().isEmpty()) {
      return new ArrayList<>();
    }

    String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
    return adRepository().findAll().stream()
        .map(Advertisement::getName)
        .filter(name -> name != null && name.toLowerCase(Locale.ROOT).contains(lowerPrefix))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(limit)
        .map(e -> {
          SearchSuggestion suggestion = new SearchSuggestion(e.getKey(), SuggestionType.AUTOCOMPLETE,
              e.getValue().doubleValue());
          suggestion.setResultsCount(e.getValue().intValue());
          return suggestion;
        })
        .collect(Collectors.toList());
  }

  /**
   * Получает подсказки категорий
   *
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit  Максимальное количество подсказок
   * @return Список подсказок категорий
   */
  public static List<SearchSuggestion> getCategorySuggestions(String prefix, int limit) {
    String effectivePrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
    return adRepository().findAll().stream()
        .map(ad -> ad.getCategory() == null ? null : ad.getCategory().getDisplayName())
        .filter(Objects::nonNull)
        .filter(cat -> effectivePrefix.isBlank() || cat.toLowerCase(Locale.ROOT).contains(effectivePrefix))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(limit)
        .map(e -> {
          SearchSuggestion suggestion = new SearchSuggestion(e.getKey(), SuggestionType.CATEGORY,
              e.getValue().doubleValue());
          suggestion.setResultsCount(e.getValue().intValue());
          suggestion.setMetadata("category");
          return suggestion;
        })
        .collect(Collectors.toList());
  }

  /**
   * Получает трендовые подсказки (популярные запросы за последнее время)
   *
   * @param prefix   Префикс для фильтрации (может быть null)
   * @param daysBack Количество дней назад для анализа трендов
   * @param limit    Максимальное количество подсказок
   * @return Список трендовых подсказок
   */
  public static List<SearchSuggestion> getTrendingSuggestions(
      String prefix, int daysBack, int limit) {
    String effectivePrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
    return historyRepository().findAll().stream()
        .filter(h -> h.getCreatedAt() != null
            && h.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(daysBack)))
        .map(SearchHistory::getSearchText)
        .filter(text -> text != null && !text.isBlank())
        .filter(text -> effectivePrefix.isBlank() || text.toLowerCase(Locale.ROOT).startsWith(effectivePrefix))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(limit)
        .map(e -> new SearchSuggestion(e.getKey(), SuggestionType.TRENDING, e.getValue().doubleValue()))
        .collect(Collectors.toList());
  }

  /**
   * Получает персонализированные подсказки на основе предпочтений пользователя
   *
   * @param userId ID пользователя
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit  Максимальное количество подсказок
   * @return Список персонализированных подсказок
   */
  public static List<SearchSuggestion> getPersonalizedSuggestions(
      UUID userId, String prefix, int limit) {
    String effectivePrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
    Map<String, Long> preferredTerms = historyRepository().findByUserIdOrderByCreatedAtDesc(userId).stream()
        .flatMap(h -> h.getCategories().stream())
        .filter(term -> term != null && !term.isBlank())
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    return adRepository().findAll().stream()
        .filter(ad -> ad.getName() != null)
        .filter(ad -> effectivePrefix.isBlank() || ad.getName().toLowerCase(Locale.ROOT).startsWith(effectivePrefix))
        .map(ad -> {
          double relevance = preferredTerms.entrySet().stream()
              .filter(e -> ad.getCategory() != null && ad.getCategory().getDisplayName().contains(e.getKey()))
              .mapToDouble(Map.Entry::getValue)
              .sum();
          return new SearchSuggestion(ad.getName(), SuggestionType.PERSONALIZED, relevance);
        })
        .sorted(Comparator.comparing(SearchSuggestion::getRelevance, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(limit)
        .collect(Collectors.toList());
  }
}
