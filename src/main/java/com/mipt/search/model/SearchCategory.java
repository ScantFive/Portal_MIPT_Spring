package com.mipt.search.model;

import lombok.Getter;
import lombok.Setter;

/** Категория для фильтрации объявлений. */
@Getter
@Setter
public class SearchCategory {
  private String categoryTitle;
  private boolean isActive;
}
