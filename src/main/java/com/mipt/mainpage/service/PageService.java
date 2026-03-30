package com.mipt.mainpage.service;

import com.mipt.mainpage.model.PageResponse;
import com.mipt.search.model.SearchQuery;
import java.util.UUID;

public interface PageService {
  PageResponse getPage(SearchQuery search, UUID userId);

  PageResponse getHomePage();
}
