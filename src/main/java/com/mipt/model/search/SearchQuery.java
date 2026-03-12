package com.mipt.model.search;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
