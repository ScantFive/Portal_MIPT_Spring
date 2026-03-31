package com.mipt.model.mainpage;

import com.mipt.model.search.SearchQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * Ответ страницы с объявлениями. Содержит информацию о пользователе, поисковом
 * запросе, списке
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
