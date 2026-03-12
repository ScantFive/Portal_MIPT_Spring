package com.mipt.model.search;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Модель истории поискового запроса. */
@Getter
@Setter
public class SearchHistory {
  private UUID id;
  private UUID userId;
  private String searchText;
  private SearchType searchType;
  private List<String> categories;
  private String filtersJson; // JSON представление фильтров
  private SearchSortOrder sortOrder;
  private Integer resultsCount;
  private LocalDateTime createdAt;

  /** Создает объект истории из поискового запроса */
  public static SearchHistory fromSearchQuery(UUID userId, SearchQuery query, int resultsCount) {
    SearchHistory history = new SearchHistory();
    history.setUserId(userId);
    history.setSearchText(query.getSearchText());
    history.setSearchType(query.getType());
    history.setSortOrder(query.getSortOrder());
    history.setResultsCount(resultsCount);

    if (query.getCategory() != null && !query.getCategory().isEmpty()) {
      history.setCategories(
          query.getCategory().stream().map(SearchCategory::getCategoryTitle).toList());
    }

    return history;
  }

  /** Преобразует историю обратно в SearchQuery */
  public SearchQuery toSearchQuery() {
    SearchQuery query = new SearchQuery();
    query.setSearchText(this.searchText);
    query.setType(this.searchType);
    query.setSortOrder(this.sortOrder);

    if (this.categories != null && !this.categories.isEmpty()) {
      List<SearchCategory> searchCategories =
          this.categories.stream()
              .map(
                  title -> {
                    SearchCategory cat = new SearchCategory();
                    cat.setCategoryTitle(title);
                    cat.setActive(true);
                    return cat;
                  })
              .toList();
      query.setCategory(searchCategories);
    }

    return query;
  }
}
