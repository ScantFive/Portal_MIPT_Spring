package com.mipt.search.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SuggestionType;
import com.mipt.search.repository.SearchHistoryRepository;
import com.mipt.search.repository.SearchRepository;
import com.mipt.search.repository.SearchSuggestionRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class SearchServiceImplTest {

  @Test
  void searchShouldSaveHistoryForAuthorizedSignificantQuery() {
    SearchServiceImpl service = new SearchServiceImpl();
    SearchQuery query = new SearchQuery();
    query.setSearchText("java");
    UUID userId = UUID.randomUUID();
    List<ShortAdvert> expected = List.of(ShortAdvert.builder().title("a").build());

    try (MockedStatic<SearchRepository> repo = Mockito.mockStatic(SearchRepository.class);
        MockedStatic<SearchHistoryRepository> history = Mockito.mockStatic(SearchHistoryRepository.class)) {
      repo.when(() -> SearchRepository.getAdverts(10L, 0L, query, userId)).thenReturn(expected);

      List<ShortAdvert> result = service.search(10, 0, query, userId);

      assertEquals(expected, result);
      history.verify(() -> SearchHistoryRepository.saveSearchHistory(userId, query, expected.size()));
    }
  }

  @Test
  void searchShouldNotSaveHistoryForAnonymousUser() {
    SearchServiceImpl service = new SearchServiceImpl();
    SearchQuery query = new SearchQuery();
    query.setSearchText("java");
    List<ShortAdvert> expected = List.of();

    try (MockedStatic<SearchRepository> repo = Mockito.mockStatic(SearchRepository.class);
        MockedStatic<SearchHistoryRepository> history = Mockito.mockStatic(SearchHistoryRepository.class)) {
      repo.when(() -> SearchRepository.getAdverts(10L, 0L, query, null)).thenReturn(expected);

      List<ShortAdvert> result = service.search(10, 0, query, null);

      assertEquals(expected, result);
      history.verifyNoInteractions();
    }
  }

  @Test
  void searchFavoritesShouldRejectNullUser() {
    SearchServiceImpl service = new SearchServiceImpl();

    assertThrows(IllegalArgumentException.class, () -> service.searchFavorites(null, new SearchQuery(), 10, 0));
  }

  @Test
  void getSearchSuggestionsShouldMergeDeduplicateAndLimit() {
    SearchServiceImpl service = new SearchServiceImpl();
    UUID userId = UUID.randomUUID();

    SearchSuggestion s1 = new SearchSuggestion("java", SuggestionType.HISTORY, 2.0);
    SearchSuggestion s2 = new SearchSuggestion("java", SuggestionType.POPULAR, 10.0);
    SearchSuggestion s3 = new SearchSuggestion("spring", SuggestionType.AUTOCOMPLETE, 5.0);

    try (MockedStatic<SearchSuggestionRepository> suggestion = Mockito.mockStatic(SearchSuggestionRepository.class)) {
      suggestion.when(() -> SearchSuggestionRepository.getHistorySuggestions(eq(userId), anyString(), anyInt()))
          .thenReturn(List.of(s1));
      suggestion.when(() -> SearchSuggestionRepository.getPersonalizedSuggestions(eq(userId), anyString(), anyInt()))
          .thenReturn(List.of());
      suggestion.when(() -> SearchSuggestionRepository.getAutocompleteSuggestions(anyString(), anyInt()))
          .thenReturn(List.of(s3));
      suggestion.when(() -> SearchSuggestionRepository.getPopularSuggestions(anyString(), anyInt()))
          .thenReturn(List.of(s2));
      suggestion.when(() -> SearchSuggestionRepository.getTrendingSuggestions(anyString(), anyInt(), anyInt()))
          .thenReturn(List.of());

      List<SearchSuggestion> result = service.getSearchSuggestions("ja", userId, 2);

      assertEquals(2, result.size());
      assertEquals("java", result.get(0).getText());
      assertEquals(10.0, result.get(0).getRelevance());
    }
  }
}
