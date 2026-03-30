package com.mipt.search.model;

import lombok.Getter;

/** Порядок сортировки объявлений. */
@Getter
public enum SearchSortOrder {
  NEWEST,
  OLDEST,
  CHEAPEST,
  EXPENSIVE
}
