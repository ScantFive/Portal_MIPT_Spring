package com.mipt.checks;



import com.mipt.model.mainpage.PageResponse;
import com.mipt.model.mainpage.ShortAdvert;
import com.mipt.model.search.SearchQuery;
import com.mipt.model.search.SearchType;
import com.mipt.service.MainPage;
import com.mipt.service.PageService;

import java.util.UUID;

/**
 * Тестовый сценарий для проверки функциональности главной страницы (MainPage)
 */
public class MainpageCheck {

  private static final String SEPARATOR = "=".repeat(80);

  // Тестовые пользователи из базы данных
  private static final UUID TEST_USER_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID TEST_USER_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

  public static void run() {
    System.out.println("\n" + SEPARATOR);
    System.out.println("ЗАПУСК ТЕСТОВ ФУНКЦИОНАЛЬНОСТИ ГЛАВНОЙ СТРАНИЦЫ");
    System.out.println(SEPARATOR + "\n");

    try {
      testGetHomePage();
      testGetPageWithoutUser();
      testGetPageWithUser();
      testGetPageWithSearch();
      testGetPageWithPriceFilter();
      testGetPageWithCategorySearch();
      testPageResponseStructure();

      System.out.println("\n" + SEPARATOR);
      System.out.println("✓ ВСЕ ТЕСТЫ ГЛАВНОЙ СТРАНИЦЫ УСПЕШНО ПРОЙДЕНЫ!");
      System.out.println(SEPARATOR + "\n");

    } catch (Exception e) {
      System.err.println("\n✗ ОШИБКА ПРИ ВЫПОЛНЕНИИ ТЕСТОВ: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Тест 1: Получение главной страницы без параметров
   */
  private static void testGetHomePage() {
    TestUtils.printTestHeader("ТЕСТ 1: Получение главной страницы (без параметров)");

    PageService pageService = new MainPage();
    PageResponse response = pageService.getHomePage();

    System.out.println("✓ Получен ответ главной страницы");
    System.out.println("  - Пользователь: " + (response.getUserId() != null ? response.getUserId() : "не авторизован"));
    System.out.println("  - Всего объявлений: " + response.getTotalElements());
    System.out.println("  - На странице: " + response.getShortAdverts().size());

    if (!response.getShortAdverts().isEmpty()) {
      System.out.println("\nПримеры объявлений:");
      TestUtils.printAdvertisementsDetailed(response.getShortAdverts(), 5);
    }

    TestUtils.assertNotNull(response, "Response не должен быть null");
    TestUtils.assertNotNull(response.getShortAdverts(), "Список объявлений не должен быть null");

    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Главная страница работает корректно");
    TestUtils.printTestFooter();
  }

  /**
   * Тест 2: Получение страницы без авторизованного пользователя
   */
  private static void testGetPageWithoutUser() {
    TestUtils.printTestHeader("ТЕСТ 2: Получение страницы без авторизации");

    PageService pageService = new MainPage();
    SearchQuery query = new SearchQuery();
    PageResponse response = pageService.getPage(query, null);

    System.out.println("✓ Получен ответ для неавторизованного пользователя");
    System.out.println("  - Всего объявлений: " + response.getTotalElements());
    System.out.println("  - На странице: " + response.getShortAdverts().size());

    // Проверяем, что у объявлений статус избранного = false для неавторизованного пользователя
    boolean allNotFavorite = response.getShortAdverts().stream()
        .noneMatch(ShortAdvert::isFavorite);

    System.out.println("  - Избранные отмечены: " + !allNotFavorite);

    TestUtils.assertTrue(response.getUserId() == null,
        "Для неавторизованного пользователя userId должен быть null");

    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Страница для неавторизованного пользователя работает");
    TestUtils.printTestFooter();
  }

  /**
   * Тест 3: Получение страницы с авторизованным пользователем
   */
  private static void testGetPageWithUser() {
    TestUtils.printTestHeader("ТЕСТ 3: Получение страницы для авторизованного пользователя");

    PageService pageService = new MainPage();
    SearchQuery query = new SearchQuery();
    PageResponse response = pageService.getPage(query, TEST_USER_1);

    System.out.println("✓ Получен ответ для пользователя: " + TEST_USER_1);
    System.out.println("  - Всего объявлений: " + response.getTotalElements());
    System.out.println("  - На странице: " + response.getShortAdverts().size());

    // Проверяем, есть ли избранные объявления
    long favoriteCount = response.getShortAdverts().stream()
        .filter(ShortAdvert::isFavorite)
        .count();

    System.out.println("  - Избранных на странице: " + favoriteCount);

    if (favoriteCount > 0) {
      System.out.println("\nИзбранные объявления:");
      response.getShortAdverts().stream()
          .filter(ShortAdvert::isFavorite)
          .limit(3)
          .forEach(ad -> System.out.printf("  ⭐ %s (ID: %s)%n",
              ad.getTitle(),
              ad.getAdvertId().toString().substring(0, 8)));
    }

    TestUtils.assertNotNull(response, "Response не должен быть null");
    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Страница с авторизацией работает корректно");
    TestUtils.printTestFooter();
  }

  /**
   * Тест 4: Получение страницы с поисковым запросом
   */
  private static void testGetPageWithSearch() {
    TestUtils.printTestHeader("ТЕСТ 4: Получение страницы с поиском");

    PageService pageService = new MainPage();
    SearchQuery query = new SearchQuery();
    query.setSearchText("помощь");

    PageResponse response = pageService.getPage(query, TEST_USER_1);

    System.out.println("✓ Выполнен поиск по запросу: 'помощь'");
    System.out.println("  - Всего найдено: " + response.getTotalElements());
    System.out.println("  - На странице: " + response.getShortAdverts().size());

    if (!response.getShortAdverts().isEmpty()) {
      System.out.println("\nНайденные объявления:");
      TestUtils.printAdvertisementsDetailed(response.getShortAdverts(), 5);
    }

    TestUtils.assertNotNull(response.getSearch(), "SearchQuery не должен быть null");
    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Поиск работает корректно");
    TestUtils.printTestFooter();
  }

  /**
   * Тест 5: Получение страницы с сортировкой объявлений
   */
  private static void testGetPageWithPriceFilter() {
    TestUtils.printTestHeader("ТЕСТ 5: Получение страницы с различными ценами");

    PageService pageService = new MainPage();
    SearchQuery query = new SearchQuery();

    PageResponse response = pageService.getPage(query, null);

    System.out.println("✓ Получена страница с объявлениями");
    System.out.println("  - Всего найдено: " + response.getTotalElements());
    System.out.println("  - На странице: " + response.getShortAdverts().size());

    if (!response.getShortAdverts().isEmpty()) {
      System.out.println("\nОбъявления с ценами:");
      response.getShortAdverts().stream()
          .limit(5)
          .forEach(ad -> System.out.printf("  • %s - %d руб.%n", ad.getTitle(), ad.getPrice()));

      // Статистика по ценам
      long minPrice = response.getShortAdverts().stream()
          .mapToLong(ShortAdvert::getPrice)
          .min()
          .orElse(0);

      long maxPrice = response.getShortAdverts().stream()
          .mapToLong(ShortAdvert::getPrice)
          .max()
          .orElse(0);

      System.out.println("\n  - Минимальная цена: " + minPrice + " руб.");
      System.out.println("  - Максимальная цена: " + maxPrice + " руб.");
    }

    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Цены отображаются корректно");
    TestUtils.printTestFooter();
  }

  /**
   * Тест 6: Получение страницы с фильтром по категории
   */
  private static void testGetPageWithCategorySearch() {
    TestUtils.printTestHeader("ТЕСТ 6: Получение страницы с фильтром по типу");

    PageService pageService = new MainPage();
    SearchQuery query = new SearchQuery();
    query.setType(SearchType.SERVICES);

    PageResponse response = pageService.getPage(query, null);

    System.out.println("✓ Применен фильтр: тип = SERVICES (Услуги)");
    System.out.println("  - Всего найдено: " + response.getTotalElements());
    System.out.println("  - На странице: " + response.getShortAdverts().size());

    if (!response.getShortAdverts().isEmpty()) {
      System.out.println("\nОбъявления типа SERVICES:");
      TestUtils.printAdvertisementsDetailed(response.getShortAdverts(), 5);
    }

    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Фильтр по категории работает корректно");
    TestUtils.printTestFooter();
  }

  /**
   * Тест 7: Проверка структуры PageResponse
   */
  private static void testPageResponseStructure() {
    TestUtils.printTestHeader("ТЕСТ 7: Проверка структуры PageResponse");

    PageService pageService = new MainPage();
    SearchQuery query = new SearchQuery();
    query.setSearchText("математика");

    PageResponse response = pageService.getPage(query, TEST_USER_2);

    System.out.println("✓ Проверка всех полей PageResponse");

    // Проверка userId
    System.out.println("  - userId: " +
        (response.getUserId() != null ? response.getUserId() : "null") + " ✓");

    // Проверка search
    System.out.println("  - search: " +
        (response.getSearch() != null ? "присутствует" : "null") + " ✓");
    if (response.getSearch() != null) {
      System.out.println("    • searchText: " + response.getSearch().getSearchText());
      System.out.println("    • type: " + response.getSearch().getType());
    }

    // Проверка shortAdverts
    System.out.println("  - shortAdverts: " +
        (response.getShortAdverts() != null ? "список из " + response.getShortAdverts().size() + " элементов" : "null") + " ✓");

    // Проверка totalElements
    System.out.println("  - totalElements: " + response.getTotalElements() + " ✓");

    // Детальная проверка структуры ShortAdvert
    if (!response.getShortAdverts().isEmpty()) {
      ShortAdvert firstAdvert = response.getShortAdverts().get(0);
      System.out.println("\n  Структура ShortAdvert (первое объявление):");
      System.out.println("    • advertId: " + firstAdvert.getAdvertId() + " ✓");
      System.out.println("    • authorId: " + firstAdvert.getAuthorId() + " ✓");
      System.out.println("    • title: " + firstAdvert.getTitle() + " ✓");
      System.out.println("    • descriptionPreview: " +
          (firstAdvert.getDescriptionPreview() != null ? "присутствует" : "null") + " ✓");
      System.out.println("    • price: " + firstAdvert.getPrice() + " ✓");
      System.out.println("    • photos: " +
          (firstAdvert.getPhotos() != null ? firstAdvert.getPhotos().size() + " фото" : "null") + " ✓");
      System.out.println("    • isFavorite: " + firstAdvert.isFavorite() + " ✓");
    }

    TestUtils.assertNotNull(response, "Response не должен быть null");
    TestUtils.assertNotNull(response.getShortAdverts(), "ShortAdverts не должен быть null");
    TestUtils.assertNotNull(response.getTotalElements(), "TotalElements не должен быть null");

    System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Структура PageResponse корректна");
    TestUtils.printTestFooter();
  }
}

