package com.mipt.search.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

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
