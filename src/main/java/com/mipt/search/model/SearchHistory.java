package com.mipt.search.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Модель истории поискового запроса. */
@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "search_text")
  private String searchText;

  @Enumerated(EnumType.STRING)
  @Column(name = "search_type")
  private SearchType searchType;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "search_history_categories", joinColumns = @JoinColumn(name = "search_history_id"))
  @Column(name = "category")
  private List<String> categories;

  @Column(name = "filters_json", columnDefinition = "TEXT")
  private String filtersJson;

  @Enumerated(EnumType.STRING)
  @Column(name = "sort_order")
  private SearchSortOrder sortOrder;

  @Column(name = "results_count")
  private Integer resultsCount;

  @Column(name = "created_at", nullable = false)
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
      List<SearchCategory> searchCategories = this.categories.stream()
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
