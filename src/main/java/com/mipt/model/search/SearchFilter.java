package com.mipt.model.search;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Фильтр для поиска по числовым параметрам (например, цена, площадь). */
@Getter
@Setter
public class SearchFilter {
  private String filterTitle;
  private BigDecimal fromBorder;
  private BigDecimal toBorder;
  private BigDecimal fromValue;
  private BigDecimal toValue;
  private boolean isActive;
}
