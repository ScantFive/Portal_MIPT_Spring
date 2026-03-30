package com.mipt.mainpage.service;

import com.mipt.mainpage.model.PageResponse;
import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.repository.SearchRepository;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Сервис главной страницы с объявлениями */
public class MainPage implements PageService {

  private static final long DEFAULT_PAGE_LIMIT = 50;
  private static final long DEFAULT_PAGE_OFFSET = 0;

  @Override
  public PageResponse getHomePage() {
    return getPage(null, null);
  }

  @Override
  public PageResponse getPage(SearchQuery search, UUID userId) {
    SearchQuery effectiveSearch = Optional.ofNullable(search).orElseGet(SearchQuery::new);

    List<ShortAdvert> shortAdvertList =
        SearchRepository.getAdverts(
            DEFAULT_PAGE_LIMIT, DEFAULT_PAGE_OFFSET, effectiveSearch, userId);

    // Получаем реальное количество объявлений, соответствующих запросу
    long totalElements = SearchRepository.getAdvertsCount(effectiveSearch);

    return PageResponse.builder()
        .userId(userId)
        .search(effectiveSearch)
        .shortAdverts(shortAdvertList)
        .totalElements(BigInteger.valueOf(totalElements))
        .build();
  }
}
