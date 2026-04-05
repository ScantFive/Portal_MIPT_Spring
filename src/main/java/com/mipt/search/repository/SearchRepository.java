package com.mipt.search.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.repository.AdvertisementRepository;
import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.mainpage.model.Favorite;
import com.mipt.mainpage.repository.FavoriteJpaRepository;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSortOrder;
import com.mipt.search.model.SearchCategory;
import com.mipt.util.SpringContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/** Репозиторий для поиска объявлений. */
public class SearchRepository {

  private static final int DESCRIPTION_PREVIEW_LENGTH = 100;

  private static AdvertisementRepository adRepository() {
    return SpringContext.getBean(AdvertisementRepository.class);
  }

  private static FavoriteJpaRepository favoriteRepository() {
    return SpringContext.getBean(FavoriteJpaRepository.class);
  }

  public static List<ShortAdvert> getAdverts(long limit, long offset, SearchQuery search) {
    return getAdverts(limit, offset, search, null);
  }

  public static List<ShortAdvert> getAdverts(
      long limit, long offset, SearchQuery search, UUID userId) {
    SearchQuery effectiveSearch = Optional.ofNullable(search).orElseGet(SearchQuery::new);
    Set<UUID> favoriteIds = userId == null
        ? Collections.emptySet()
        : favoriteRepository().findByUserId(userId).stream()
            .map(Favorite::getAdvertisementId)
            .collect(Collectors.toSet());

    return adRepository().findAll().stream()
        .filter(ad -> matchesSearch(ad, effectiveSearch))
        .sorted(buildComparator(effectiveSearch.getSortOrder()))
        .skip(offset)
        .limit(limit)
        .map(ad -> toShortAdvert(ad, favoriteIds.contains(ad.getId())))
        .collect(Collectors.toList());
  }

  /** Получить общее количество объявлений, соответствующих поисковому запросу */
  public static long getAdvertsCount(SearchQuery search) {
    SearchQuery effectiveSearch = Optional.ofNullable(search).orElseGet(SearchQuery::new);
    return adRepository().findAll().stream().filter(ad -> matchesSearch(ad, effectiveSearch)).count();
  }

  /** Получить избранные объявления пользователя с возможностью фильтрации */
  public static List<ShortAdvert> getFavoriteAdverts(
      UUID userId, long limit, long offset, SearchQuery search) {
    SearchQuery effectiveSearch = Optional.ofNullable(search).orElseGet(SearchQuery::new);
    Set<UUID> favoriteIds = favoriteRepository().findByUserId(userId).stream()
        .map(Favorite::getAdvertisementId)
        .collect(Collectors.toSet());

    return adRepository().findAllById(favoriteIds).stream()
        .filter(ad -> matchesSearch(ad, effectiveSearch))
        .sorted(buildComparator(effectiveSearch.getSortOrder()))
        .skip(offset)
        .limit(limit)
        .map(ad -> toShortAdvert(ad, true))
        .collect(Collectors.toList());
  }

  private static boolean matchesSearch(Advertisement ad, SearchQuery query) {
    if (query == null) {
      return true;
    }

    if (query.getSearchText() != null && !query.getSearchText().isBlank()) {
      String text = query.getSearchText().toLowerCase(Locale.ROOT);
      String haystack = ((ad.getName() == null ? "" : ad.getName()) + " "
          + (ad.getDescription() == null ? "" : ad.getDescription()) + " "
          + (ad.getCategory() == null ? "" : ad.getCategory().getDisplayName()))
          .toLowerCase(Locale.ROOT);
      if (!haystack.contains(text)) {
        return false;
      }
    }

    if (query.getType() != null && ad.getType() != null) {
      if (!ad.getType().name().equals(query.getType().name())) {
        return false;
      }
    }

    if (query.getCategory() != null && !query.getCategory().isEmpty()) {
      boolean categoryMatch = query.getCategory().stream()
          .filter(SearchCategory::isActive)
          .map(SearchCategory::getCategoryTitle)
          .filter(Objects::nonNull)
          .anyMatch(title -> ad.getCategory() != null
              && ad.getCategory().getDisplayName().toLowerCase(Locale.ROOT)
                  .contains(title.toLowerCase(Locale.ROOT)));
      if (!categoryMatch) {
        return false;
      }
    }

    return true;
  }

  private static Comparator<Advertisement> buildComparator(SearchSortOrder sortOrder) {
    if (sortOrder == null || sortOrder == SearchSortOrder.NEWEST) {
      return Comparator.comparing(Advertisement::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
          .reversed();
    }
    if (sortOrder == SearchSortOrder.OLDEST) {
      return Comparator.comparing(Advertisement::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }
    if (sortOrder == SearchSortOrder.CHEAPEST) {
      return Comparator.comparing(Advertisement::getPrice, Comparator.nullsLast(Comparator.naturalOrder()));
    }
    return Comparator.comparing(Advertisement::getPrice, Comparator.nullsLast(Comparator.naturalOrder()))
        .reversed();
  }

  private static ShortAdvert toShortAdvert(Advertisement ad, boolean isFavorite) {
    List<URL> photos = new ArrayList<>();
    if (ad.getPhotoUrls() != null) {
      for (String url : ad.getPhotoUrls()) {
        try {
          photos.add(new URL(url));
        } catch (MalformedURLException ignored) {
          // Ignore malformed URLs in legacy data.
        }
      }
    }

    String description = ad.getDescription() == null ? "" : ad.getDescription();
    String preview = description.length() > DESCRIPTION_PREVIEW_LENGTH
        ? description.substring(0, DESCRIPTION_PREVIEW_LENGTH) + "..."
        : description;

    return ShortAdvert.builder()
        .advertId(ad.getId())
        .authorId(ad.getAuthorId())
        .title(ad.getName())
        .descriptionPreview(preview)
        .price(ad.getPrice() == null ? 0L : ad.getPrice())
        .photos(photos)
        .isFavorite(isFavorite)
        .build();
  }
}
