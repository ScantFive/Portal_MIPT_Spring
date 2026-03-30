package com.mipt.mainpage.model;

import com.mipt.search.model.SearchQuery;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Ответ страницы с объявлениями. Содержит информацию о пользователе, поисковом запросе, списке
 * объявлений и общем количестве.
 */
@Data
@Builder
@AllArgsConstructor
public class PageResponse {
  private UUID userId;
  private SearchQuery search;
  private List<ShortAdvert> shortAdverts;
  private BigInteger totalElements;
}
