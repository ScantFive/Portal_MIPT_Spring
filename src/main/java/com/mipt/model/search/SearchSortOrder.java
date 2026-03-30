package com.mipt.model.search;

import lombok.Getter;

/** Порядок сортировки объявлений. */
@Getter
public enum SearchSortOrder {
  NEWEST,
  OLDEST,
  CHEAPEST,
  EXPENSIVE
}
