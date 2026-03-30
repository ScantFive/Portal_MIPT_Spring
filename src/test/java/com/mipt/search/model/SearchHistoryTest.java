package com.mipt.search.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SearchHistoryTest {

  @Test
  void fromSearchQueryShouldMapBasicFields() {
    UUID userId = UUID.randomUUID();
    SearchQuery query = new SearchQuery();
    query.setSearchText("java");
    query.setType(SearchType.SERVICES);
    query.setSortOrder(SearchSortOrder.NEWEST);

    SearchCategory category = new SearchCategory();
    category.setCategoryTitle("Услуги/Образование");
    category.setActive(true);
    query.setCategory(List.of(category));

    SearchHistory history = SearchHistory.fromSearchQuery(userId, query, 12);

    assertEquals(userId, history.getUserId());
    assertEquals("java", history.getSearchText());
    assertEquals(SearchType.SERVICES, history.getSearchType());
    assertEquals(SearchSortOrder.NEWEST, history.getSortOrder());
    assertEquals(12, history.getResultsCount());
    assertEquals(List.of("Услуги/Образование"), history.getCategories());
  }

  @Test
  void toSearchQueryShouldRestoreMappedData() {
    SearchHistory history = new SearchHistory();
    history.setSearchText("spring");
    history.setSearchType(SearchType.OBJECTS);
    history.setSortOrder(SearchSortOrder.CHEAPEST);
    history.setCategories(List.of("Товары/Книги"));

    SearchQuery query = history.toSearchQuery();

    assertEquals("spring", query.getSearchText());
    assertEquals(SearchType.OBJECTS, query.getType());
    assertEquals(SearchSortOrder.CHEAPEST, query.getSortOrder());
    assertNotNull(query.getCategory());
    assertEquals(1, query.getCategory().size());
    assertEquals("Товары/Книги", query.getCategory().get(0).getCategoryTitle());
    assertTrue(query.getCategory().get(0).isActive());
  }
}
