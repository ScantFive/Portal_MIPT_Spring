package com.mipt.search.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Модель поискового запроса с параметрами фильтрации и сортировки. */
@Getter
@Setter
public class SearchQuery {
  private String searchText;
  private SearchSortOrder sortOrder;
  private SearchType type;
  private List<SearchCategory> category;
  private List<SearchFilter> filters;
}
