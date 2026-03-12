package com.mipt.service;

import com.mipt.model.mainpage.PageResponse;
import com.mipt.model.mainpage.ShortAdvert;
import com.mipt.model.search.SearchQuery;
import com.mipt.repository.search.SearchRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Service
@RequiredArgsConstructor
@Transactional
// Сервис главной страницы с объявлениями
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
