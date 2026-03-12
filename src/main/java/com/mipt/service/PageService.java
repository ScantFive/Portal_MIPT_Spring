package com.mipt.service;

import com.mipt.model.mainpage.PageResponse;
import com.mipt.model.search.SearchQuery;

import java.util.UUID;

public interface PageService {
  PageResponse getPage(SearchQuery search, UUID userId);

  PageResponse getHomePage();
}
