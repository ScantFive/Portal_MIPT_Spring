package com.mipt.search.service;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.event.SearchHistoryEvent;
import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SearchType;
import com.mipt.search.repository.SearchHistoryRepository;
import com.mipt.search.repository.SearchRepository;
import com.mipt.search.repository.SearchSuggestionRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchServiceImpl implements SearchService {

  private final SearchKafkaEventPublisher eventPublisher;

  @Override
  public List<ShortAdvert> search(long limit, long offset, SearchQuery query) {
    return search(limit, offset, query, null);
  }

  @Override
  public List<ShortAdvert> search(long limit, long offset, SearchQuery query, UUID userId) {
    SearchQuery effectiveQuery = Optional.ofNullable(query).orElseGet(SearchQuery::new);
    List<ShortAdvert> adverts = SearchRepository.getAdverts(limit, offset, effectiveQuery, userId);

    if (userId != null && isSignificantQuery(effectiveQuery)) {
      SearchHistoryRepository.saveSearchHistory(userId, effectiveQuery, adverts.size());
      eventPublisher.publish(SearchHistoryEvent.performed(userId, effectiveQuery, adverts.size()));
    }

    return adverts;
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
    com.mipt.search.model.SearchCategory category = new com.mipt.search.model.SearchCategory();
    category.setCategoryTitle(categoryTitle);
    category.setActive(true);
    query.setCategory(Collections.singletonList(category));
    return search(limit, offset, query);
  }

  @Override
  public List<ShortAdvert> searchFavorites(UUID userId, SearchQuery query, long limit, long offset) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID не может быть null для поиска в избранном");
    }

    SearchQuery effectiveQuery = Optional.ofNullable(query).orElseGet(SearchQuery::new);
    List<ShortAdvert> adverts = SearchRepository.getFavoriteAdverts(userId, limit, offset, effectiveQuery);

    if (isSignificantQuery(effectiveQuery)) {
      SearchHistoryRepository.saveSearchHistory(userId, effectiveQuery, adverts.size());
    }

    return adverts;
  }

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

  @Override
  public List<SearchSuggestion> getSearchSuggestions(String prefix, UUID userId, int limit) {
    List<SearchSuggestion> allSuggestions = new ArrayList<>();

    int historyLimit = Math.max(1, limit / 4);
    int autocompleteLimit = Math.max(1, limit / 3);
    int popularLimit = Math.max(1, limit / 4);
    int trendingLimit = Math.max(1, limit / 4);

    if (userId != null) {
      allSuggestions.addAll(getHistorySuggestions(userId, prefix, historyLimit));
      allSuggestions.addAll(getPersonalizedSuggestions(userId, prefix, Math.max(1, historyLimit / 2)));
    }

    if (prefix != null && !prefix.trim().isEmpty()) {
      allSuggestions.addAll(getAutocompleteSuggestions(prefix, autocompleteLimit));
    }

    allSuggestions.addAll(getPopularSuggestions(prefix, popularLimit));
    allSuggestions.addAll(getTrendingSuggestions(prefix, trendingLimit));

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

    return uniqueSuggestions.values().stream()
        .sorted((s1, s2) -> {
          double r1 = s1.getRelevance() == null ? 0.0 : s1.getRelevance();
          double r2 = s2.getRelevance() == null ? 0.0 : s2.getRelevance();
          return Double.compare(r2, r1);
        })
        .limit(limit)
        .toList();
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
    return SearchSuggestionRepository.getTrendingSuggestions(prefix, 7, limit);
  }

  @Override
  public List<SearchSuggestion> getPersonalizedSuggestions(UUID userId, String prefix, int limit) {
    if (userId == null) {
      return Collections.emptyList();
    }
    return SearchSuggestionRepository.getPersonalizedSuggestions(userId, prefix, limit);
  }

  private boolean isSignificantQuery(SearchQuery query) {
    return (query.getSearchText() != null && !query.getSearchText().trim().isEmpty())
        || query.getType() != null
        || (query.getCategory() != null && !query.getCategory().isEmpty())
        || (query.getFilters() != null && !query.getFilters().isEmpty());
  }
}
