package com.mipt.service.checks;



import com.mipt.model.mainpage.PageResponse;
import com.mipt.model.mainpage.ShortAdvert;
import com.mipt.model.search.SearchQuery;
import com.mipt.model.search.SearchType;
import com.mipt.service.FavoritesService;
import com.mipt.service.search.SearchService;
import com.mipt.service.search.SearchServiceImpl;

import java.util.List;
import java.util.UUID;

/** Тестовый сценарий для проверки функциональности избранного */
public class FavoritesCheck {

  private static final String SEPARATOR = "=".repeat(80);
  private static final long DEFAULT_LIMIT = 50;
  private static final long DEFAULT_OFFSET = 0;

  // Тестовые пользователи из базы данных
  private static final UUID TEST_USER_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID TEST_USER_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

  public static void run() {
    System.out.println("\n" + SEPARATOR);
    System.out.println("ЗАПУСК ТЕСТОВ ФУНКЦИОНАЛЬНОСТИ ИЗБРАННОГО");
    System.out.println(SEPARATOR + "\n");

    try {
      testAddToFavorites();
      testGetFavorites();
      testSearchInFavorites();
      testFavoritesPageService();
      testRemoveFromFavorites();
      testFavoritesCount();

      System.out.println("\n" + SEPARATOR);
      System.out.println("✓ ВСЕ ТЕСТЫ ИЗБРАННОГО УСПЕШНО ПРОЙДЕНЫ!");
      System.out.println(SEPARATOR + "\n");

    } catch (Exception e) {
      System.err.println("\n✗ ОШИБКА ПРИ ВЫПОЛНЕНИИ ТЕСТОВ: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void testAddToFavorites() {
    TestUtils.printTestHeader("ТЕСТ 1: Добавление объявления в избранное");

    SearchService searchService = new SearchServiceImpl();
    FavoritesService favoritesService = new FavoritesService();

    // Получаем первое объявление
    List<ShortAdvert> adverts = searchService.search(1, 0, new SearchQuery());
    if (adverts.isEmpty()) {
      System.out.println("✗ Нет объявлений для теста");
      return;
    }

    ShortAdvert advert = adverts.get(0);
    System.out.println("Объявление: " + advert.getTitle() + " (ID: " + advert.getAdvertId() + ")");

    // Добавляем в избранное
    favoritesService.addToFavorites(TEST_USER_1, advert.getAdvertId());
    System.out.println("✓ Добавлено в избранное для пользователя " + TEST_USER_1);

    // Проверяем, что добавлено
    boolean isFavorite = favoritesService.isFavorite(TEST_USER_1, advert.getAdvertId());
    System.out.println("✓ Проверка: объявление " + (isFavorite ? "в избранном" : "НЕ в избранном"));

    TestUtils.printTestFooter();
  }

  private static void testGetFavorites() {
    TestUtils.printTestHeader("ТЕСТ 2: Получение списка избранных объявлений");

    SearchService searchService = new SearchServiceImpl();
    FavoritesService favoritesService = new FavoritesService();

    // Добавляем несколько объявлений в избранное
    List<ShortAdvert> allAdverts = searchService.search(3, 0, new SearchQuery());
    System.out.println(
        "Добавляем " + allAdverts.size() + " объявлений в избранное для " + TEST_USER_1 + "...");

    for (ShortAdvert advert : allAdverts) {
      favoritesService.addToFavorites(TEST_USER_1, advert.getAdvertId());
    }

    // Получаем избранное через SearchService
    List<ShortAdvert> favorites =
        searchService.searchFavorites(
            TEST_USER_1, new SearchQuery(), DEFAULT_LIMIT, DEFAULT_OFFSET);

    System.out.println("✓ Получено избранных объявлений: " + favorites.size());
    TestUtils.printAdvertisementsDetailed(favorites, 5);

    // Проверяем, что все помечены как избранные
    boolean allMarkedAsFavorite = favorites.stream().allMatch(ShortAdvert::isFavorite);
    System.out.println("✓ Все объявления помечены как избранные: " + allMarkedAsFavorite);

    TestUtils.printTestFooter();
  }

  private static void testSearchInFavorites() {
    TestUtils.printTestHeader("ТЕСТ 3: Поиск в избранном по тексту");

    SearchService searchService = new SearchServiceImpl();

    // Создаём запрос с текстовым поиском
    SearchQuery query = new SearchQuery();
    query.setSearchText("SQL");

    List<ShortAdvert> results =
        searchService.searchFavorites(TEST_USER_1, query, DEFAULT_LIMIT, DEFAULT_OFFSET);

    System.out.println("--- Поиск 'SQL' в избранном ---");
    System.out.println("✓ Найдено объявлений: " + results.size());
    TestUtils.printAdvertisementsDetailed(results, 3);

    TestUtils.printTestFooter();
  }

  private static void testFavoritesPageService() {
    TestUtils.printTestHeader("ТЕСТ 4: FavoritesPageService - страница избранного");

    FavoritesService pageService = new FavoritesService();

    // Создаём поисковый запрос с фильтром по типу
    SearchQuery query = new SearchQuery();
    query.setType(SearchType.OBJECTS);

    PageResponse page = pageService.getPage(query, TEST_USER_1);

    System.out.println("User ID: " + page.getUserId());
    System.out.println("✓ Избранных товаров: " + page.getShortAdverts().size());
    System.out.println("✓ Всего избранных: " + page.getTotalElements());
    TestUtils.printAdvertisementsDetailed(page.getShortAdverts(), 5);

    TestUtils.printTestFooter();
  }

  private static void testRemoveFromFavorites() {
    TestUtils.printTestHeader("ТЕСТ 5: Удаление объявления из избранного");

    SearchService searchService = new SearchServiceImpl();
    FavoritesService favoritesService = new FavoritesService();

    // Получаем избранное
    List<ShortAdvert> favorites =
        searchService.searchFavorites(
            TEST_USER_1, new SearchQuery(), DEFAULT_LIMIT, DEFAULT_OFFSET);

    if (favorites.isEmpty()) {
      System.out.println("✗ Нет избранных объявлений для удаления");
      TestUtils.printTestFooter();
      return;
    }

    ShortAdvert toRemove = favorites.get(0);
    System.out.println("Удаляем из избранного: " + toRemove.getTitle());

    favoritesService.removeFromFavorites(TEST_USER_1, toRemove.getAdvertId());
    System.out.println("✓ Удалено из избранного");

    // Проверяем, что удалено
    boolean stillFavorite = favoritesService.isFavorite(TEST_USER_1, toRemove.getAdvertId());
    System.out.println(
        "✓ Проверка: объявление " + (stillFavorite ? "ВСЁ ЕЩЁ в избранном (ОШИБКА!)" : "удалено"));

    TestUtils.printTestFooter();
  }

  private static void testFavoritesCount() {
    TestUtils.printTestHeader("ТЕСТ 6: Подсчёт количества избранных");

    FavoritesService favoritesService = new FavoritesService();

    long count = favoritesService.getFavoritesCount(TEST_USER_1);
    System.out.println("✓ Количество избранных у пользователя " + TEST_USER_1 + ": " + count);

    // Проверяем для второго пользователя
    long countUser2 = favoritesService.getFavoritesCount(TEST_USER_2);
    System.out.println("✓ Количество избранных у пользователя " + TEST_USER_2 + ": " + countUser2);

    TestUtils.printTestFooter();
  }
}
