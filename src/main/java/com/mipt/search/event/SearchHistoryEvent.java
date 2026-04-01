package com.mipt.search.event;

import com.mipt.search.model.SearchQuery;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryEvent {
 private String eventType;
 private UUID userId;
 private String searchQuery;
 private String category;
 private String filters;
 private Integer resultsCount;
 private Instant timestamp;

 public static SearchHistoryEvent performed(UUID userId, SearchQuery query, int resultsCount) {
  String queryText = query != null ? query.getSearchText() : null;
  String category = (query != null && query.getCategory() != null && !query.getCategory().isEmpty())
    ? query.getCategory().get(0).getCategoryTitle()
    : null;
  String filters = (query != null && query.getFilters() != null) ? query.getFilters().toString() : null;

  return SearchHistoryEvent.builder()
    .eventType("SEARCH_PERFORMED")
    .userId(userId)
    .searchQuery(queryText)
    .category(category)
    .filters(filters)
    .resultsCount(resultsCount)
    .timestamp(Instant.now())
    .build();
 }
}
