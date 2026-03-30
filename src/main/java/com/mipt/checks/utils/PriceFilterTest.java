package com.mipt.checks.utils;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.SearchFilter;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.service.SearchService;
import com.mipt.search.service.SearchServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Комплексный тест для проверки фильтрации по цене.
 * Проверяет:
 * - Фильтрацию по минимальной цене
 * - Фильтрацию по максимальной цене
 * - Фильтрацию по диапазону цен
 * - Комбинацию фильтра по цене с текстовым поиском
 * - Комбинацию фильтра по цене с категориями
 * - Граничные значения
 * - Обработку некорректных значений
 */
public class PriceFilterTest {

  private static final SearchService searchService = new SearchServiceImpl();
  private static int totalTests = 0;
  private static int passedTests = 0;
  private static int failedTests = 0;

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("КОМПЛЕКСНОЕ ТЕСТИРОВАНИЕ ФИЛЬТРАЦИИ ПО ЦЕНЕ");
    System.out.println("=".repeat(80));
    System.out.println();

    UUID testUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    // Запускаем все тесты
    testMinPriceFilter();
    testMaxPriceFilter();
    testPriceRangeFilter();
    testPriceFilterWithText();
    testPriceFilterWithCategory();
    testPriceFilterWithType();
    testMultiplePriceFilters();
    testCombinedFilters();
    testBoundaryValues();
    testZeroPrice();
    testNegativePrice();
    testVeryLargePrice();
    testInactivePriceFilter();
    testEmptyPriceFilter();
    testPriceFilterWithSorting();
    testPriceFilterWithPagination();
    testPriceFilterPersistence(testUserId);

    // Итоговая статистика
    printFinalStatistics();
  }

  // ============================================================================
  // ТЕСТЫ БАЗОВОЙ ФИЛЬТРАЦИИ ПО ЦЕНЕ
  // ============================================================================

  private static void testMinPriceFilter() {
    TestUtils.printTestHeader("Фильтрация по минимальной цене");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("500"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено объявлений с ценой >= 500: " + results.size());

      // Проверяем, что все результаты соответствуют фильтру
      for (ShortAdvert advert : results) {
        if (advert.getPrice() < 500) {
          throw new AssertionError("Найдено объявление с ценой < 500: " + advert.getPrice());
        }
      }
      System.out.println("✓ Все результаты соответствуют минимальной цене");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при фильтрации по минимальной цене: " + e.getMessage());
    }
  }

  private static void testMaxPriceFilter() {
    TestUtils.printTestHeader("Фильтрация по максимальной цене");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setToValue(new BigDecimal("2000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено объявлений с ценой <= 2000: " + results.size());

      // Проверяем, что все результаты соответствуют фильтру
      for (ShortAdvert advert : results) {
        if (advert.getPrice() > 2000) {
          throw new AssertionError("Найдено объявление с ценой > 2000: " + advert.getPrice());
        }
      }
      System.out.println("✓ Все результаты соответствуют максимальной цене");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при фильтрации по максимальной цене: " + e.getMessage());
    }
  }

  private static void testPriceRangeFilter() {
    TestUtils.printTestHeader("Фильтрация по диапазону цен");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("500"));
      priceFilter.setToValue(new BigDecimal("2000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено объявлений с ценой 500-2000: " + results.size());

      // Проверяем, что все результаты соответствуют фильтру
      for (ShortAdvert advert : results) {
        long price = advert.getPrice();
        if (price < 500 || price > 2000) {
          throw new AssertionError("Найдено объявление с ценой вне диапазона: " + price);
        }
      }
      System.out.println("✓ Все результаты в диапазоне 500-2000");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при фильтрации по диапазону цен: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ КОМБИНИРОВАННОЙ ФИЛЬТРАЦИИ
  // ============================================================================

  private static void testPriceFilterWithText() {
    TestUtils.printTestHeader("Фильтр по цене + текстовый поиск");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("учебник");

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("500"));
      priceFilter.setToValue(new BigDecimal("1500"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено 'учебник' с ценой 500-1500: " + results.size());

      // Проверяем соответствие фильтру
      for (ShortAdvert advert : results) {
        long price = advert.getPrice();
        assertTrue(price >= 500, "Цена должна быть >= 500");
        assertTrue(price <= 1500, "Цена должна быть <= 1500");
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске (текст + цена): " + e.getMessage());
    }
  }

  private static void testPriceFilterWithCategory() {
    TestUtils.printTestHeader("Фильтр по цене + категория");

    try {
      SearchQuery query = new SearchQuery();

      com.mipt.search.model.SearchCategory category = new com.mipt.search.model.SearchCategory();
      category.setCategoryTitle("Товары/Книги");
      category.setActive(true);
      query.setCategory(Collections.singletonList(category));

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("100"));
      priceFilter.setToValue(new BigDecimal("5000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено в категории 'Товары/Книги' с ценой 100-5000: " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске (категория + цена): " + e.getMessage());
    }
  }

  private static void testPriceFilterWithType() {
    TestUtils.printTestHeader("Фильтр по цене + тип объявления");

    try {
      SearchQuery query = new SearchQuery();
      query.setType(com.mipt.search.model.SearchType.SERVICES);

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("1000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено услуг с ценой >= 1000: " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске (тип + цена): " + e.getMessage());
    }
  }

  private static void testMultiplePriceFilters() {
    TestUtils.printTestHeader("Несколько фильтров по цене (не должно работать корректно)");

    try {
      SearchQuery query = new SearchQuery();

      List<SearchFilter> filters = new ArrayList<>();

      SearchFilter priceFilter1 = new SearchFilter();
      priceFilter1.setFilterTitle("price");
      priceFilter1.setFromValue(new BigDecimal("500"));
      priceFilter1.setActive(true);
      filters.add(priceFilter1);

      SearchFilter priceFilter2 = new SearchFilter();
      priceFilter2.setFilterTitle("price");
      priceFilter2.setToValue(new BigDecimal("2000"));
      priceFilter2.setActive(true);
      filters.add(priceFilter2);

      query.setFilters(filters);

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с двумя фильтрами по цене: " + results.size());
      System.out.println("  (Примечание: поведение зависит от реализации QueryBuilder)");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при множественных фильтрах по цене: " + e.getMessage());
    }
  }

  private static void testCombinedFilters() {
    TestUtils.printTestHeader("Комплексный запрос: текст + категория + тип + цена + сортировка");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("программирование");
      query.setType(com.mipt.search.model.SearchType.SERVICES);
      query.setSortOrder(com.mipt.search.model.SearchSortOrder.EXPENSIVE);

      com.mipt.search.model.SearchCategory category = new com.mipt.search.model.SearchCategory();
      category.setCategoryTitle("Услуги");
      category.setActive(true);
      query.setCategory(Collections.singletonList(category));

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("1000"));
      priceFilter.setToValue(new BigDecimal("5000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Комплексный поиск: найдено " + results.size());

      // Проверяем сортировку по убыванию цены
      if (results.size() > 1) {
        for (int i = 0; i < results.size() - 1; i++) {
          long price1 = results.get(i).getPrice();
          long price2 = results.get(i + 1).getPrice();
          assertTrue(price1 >= price2,
            "Результаты должны быть отсортированы по убыванию цены");
        }
        System.out.println("✓ Сортировка по убыванию цены работает корректно");
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комплексном поиске: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ
  // ============================================================================

  private static void testBoundaryValues() {
    TestUtils.printTestHeader("Граничные значения цен");

    try {
      // Точное совпадение с ценой
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("1000"));
      priceFilter.setToValue(new BigDecimal("1000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с точной ценой 1000: " + results.size());

      // Проверяем точное совпадение
      for (ShortAdvert advert : results) {
        if (advert.getPrice() != 1000) {
          throw new AssertionError("Найдено объявление с ценой != 1000: " + advert.getPrice());
        }
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании граничных значений: " + e.getMessage());
    }
  }

  private static void testZeroPrice() {
    TestUtils.printTestHeader("Фильтрация с нулевой ценой");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(BigDecimal.ZERO);
      priceFilter.setToValue(new BigDecimal("100"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с ценой 0-100: " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании нулевой цены: " + e.getMessage());
    }
  }

  private static void testNegativePrice() {
    TestUtils.printTestHeader("Фильтрация с отрицательной ценой (некорректные данные)");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("-100"));
      priceFilter.setToValue(new BigDecimal("1000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с отрицательной ценой: " + results.size());
      System.out.println("  (Примечание: система должна обрабатывать или игнорировать некорректные значения)");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании отрицательной цены: " + e.getMessage());
    }
  }

  private static void testVeryLargePrice() {
    TestUtils.printTestHeader("Фильтрация с очень большой ценой");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("1000000000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с ценой >= 1 млрд: " + results.size());
      System.out.println("  (Ожидается 0 результатов или очень мало)");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании очень большой цены: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ АКТИВАЦИИ ФИЛЬТРОВ
  // ============================================================================

  private static void testInactivePriceFilter() {
    TestUtils.printTestHeader("Неактивный фильтр по цене");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("5000"));
      priceFilter.setToValue(new BigDecimal("10000"));
      priceFilter.setActive(false); // Неактивный фильтр
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results1 = searchService.search(100, 0, query);
      assertNotNull(results1, "Результаты не должны быть null");

      // Сравниваем с поиском без фильтра
      List<ShortAdvert> results2 = searchService.search(100, 0, null);

      System.out.println("✓ С неактивным фильтром: " + results1.size());
      System.out.println("✓ Без фильтра: " + results2.size());
      System.out.println("  (Количество результатов должно совпадать)");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании неактивного фильтра: " + e.getMessage());
    }
  }

  private static void testEmptyPriceFilter() {
    TestUtils.printTestHeader("Пустой фильтр по цене");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      // Не устанавливаем fromValue и toValue
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с пустым активным фильтром: " + results.size());
      System.out.println("  (Фильтр должен игнорироваться, если нет значений)");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании пустого фильтра: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ СОРТИРОВКИ И ПАГИНАЦИИ
  // ============================================================================

  private static void testPriceFilterWithSorting() {
    TestUtils.printTestHeader("Фильтр по цене с различными видами сортировки");

    try {
      SearchQuery baseQuery = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("100"));
      priceFilter.setToValue(new BigDecimal("5000"));
      priceFilter.setActive(true);
      baseQuery.setFilters(Collections.singletonList(priceFilter));

      // Тест сортировки по возрастанию цены
      SearchQuery query1 = copyQuery(baseQuery);
      query1.setSortOrder(com.mipt.search.model.SearchSortOrder.CHEAPEST);
      List<ShortAdvert> results1 = searchService.search(10, 0, query1);
      System.out.println("✓ Сортировка CHEAPEST: " + results1.size() + " результатов");
      if (results1.size() > 1) {
        TestUtils.printPriceSequence(results1, 3);
      }

      // Тест сортировки по убыванию цены
      SearchQuery query2 = copyQuery(baseQuery);
      query2.setSortOrder(com.mipt.search.model.SearchSortOrder.EXPENSIVE);
      List<ShortAdvert> results2 = searchService.search(10, 0, query2);
      System.out.println("✓ Сортировка EXPENSIVE: " + results2.size() + " результатов");
      if (results2.size() > 1) {
        TestUtils.printPriceSequence(results2, 3);
      }

      // Тест сортировки по дате
      SearchQuery query3 = copyQuery(baseQuery);
      query3.setSortOrder(com.mipt.search.model.SearchSortOrder.NEWEST);
      List<ShortAdvert> results3 = searchService.search(10, 0, query3);
      System.out.println("✓ Сортировка NEWEST: " + results3.size() + " результатов");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании сортировки: " + e.getMessage());
    }
  }

  private static void testPriceFilterWithPagination() {
    TestUtils.printTestHeader("Фильтр по цене с пагинацией");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("100"));
      priceFilter.setToValue(new BigDecimal("10000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      // Первая страница
      List<ShortAdvert> page1 = searchService.search(5, 0, query);
      System.out.println("✓ Страница 1 (limit=5, offset=0): " + page1.size());

      // Вторая страница
      List<ShortAdvert> page2 = searchService.search(5, 5, query);
      System.out.println("✓ Страница 2 (limit=5, offset=5): " + page2.size());

      // Третья страница
      List<ShortAdvert> page3 = searchService.search(5, 10, query);
      System.out.println("✓ Страница 3 (limit=5, offset=10): " + page3.size());

      // Проверяем, что страницы не пересекаются
      if (!page1.isEmpty() && !page2.isEmpty()) {
        UUID firstId = page1.get(0).getAdvertId();
        boolean overlap = page2.stream().anyMatch(ad -> ad.getAdvertId().equals(firstId));
        assertTrue(!overlap, "Страницы не должны пересекаться");
        System.out.println("✓ Страницы не пересекаются");
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании пагинации: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ СОХРАНЕНИЯ В ИСТОРИИ
  // ============================================================================

  private static void testPriceFilterPersistence(UUID userId) {
    TestUtils.printTestHeader("Сохранение фильтра по цене в истории поиска");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("тест цены");

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new BigDecimal("500"));
      priceFilter.setToValue(new BigDecimal("3000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      // Выполняем поиск с userId для сохранения в историю
      List<ShortAdvert> results = searchService.search(10, 0, query, userId);
      System.out.println("✓ Поиск выполнен: " + results.size() + " результатов");

      // Получаем историю
      List<com.mipt.search.model.SearchHistory> history =
        searchService.getUserSearchHistory(userId, 5);
      System.out.println("✓ Записей в истории: " + history.size());

      // Проверяем наличие последнего запроса
      if (!history.isEmpty()) {
        com.mipt.search.model.SearchHistory lastSearch = history.get(0);
        System.out.println("✓ Последний запрос: " + lastSearch.getSearchText());
        System.out.println("  Количество результатов: " + lastSearch.getResultsCount());
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при тестировании сохранения в истории: " + e.getMessage());
    }
  }

  // ============================================================================
  // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
  // ============================================================================

  private static SearchQuery copyQuery(SearchQuery original) {
    SearchQuery copy = new SearchQuery();
    copy.setSearchText(original.getSearchText());
    copy.setType(original.getType());
    copy.setCategory(original.getCategory());
    copy.setSortOrder(original.getSortOrder());

    if (original.getFilters() != null) {
      List<SearchFilter> copiedFilters = new ArrayList<>();
      for (SearchFilter filter : original.getFilters()) {
        SearchFilter copiedFilter = new SearchFilter();
        copiedFilter.setFilterTitle(filter.getFilterTitle());
        copiedFilter.setFromValue(filter.getFromValue());
        copiedFilter.setToValue(filter.getToValue());
        copiedFilter.setActive(filter.isActive());
        copiedFilters.add(copiedFilter);
      }
      copy.setFilters(copiedFilters);
    }

    return copy;
  }

  private static void assertNotNull(Object obj, String message) {
    totalTests++;
    TestUtils.assertNotNull(obj, message);
  }

  private static void assertTrue(boolean condition, String message) {
    totalTests++;
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void testPassed() {
    passedTests++;
    System.out.println("✅ ТЕСТ ПРОЙДЕН");
  }

  private static void testFailed(String message) {
    failedTests++;
    System.out.println("❌ ТЕСТ НЕ ПРОЙДЕН: " + message);
  }

  private static void printFinalStatistics() {
    System.out.println();
    System.out.println("=".repeat(80));
    System.out.println("ИТОГОВАЯ СТАТИСТИКА - ТЕСТЫ ФИЛЬТРАЦИИ ПО ЦЕНЕ");
    System.out.println("=".repeat(80));
    System.out.println("Всего тестов: " + (passedTests + failedTests));
    System.out.println("Пройдено: " + passedTests + " ✅");
    System.out.println("Не пройдено: " + failedTests + " ❌");
    System.out.println("Проверок (assertions): " + totalTests);

    double successRate = (passedTests + failedTests) > 0
        ? (passedTests * 100.0) / (passedTests + failedTests)
        : 0;
    System.out.printf("Процент успеха: %.2f%%%n", successRate);
    System.out.println("=".repeat(80));

    if (failedTests == 0) {
      System.out.println("🎉 ВСЕ ТЕСТЫ ФИЛЬТРАЦИИ ПО ЦЕНЕ УСПЕШНО ПРОЙДЕНЫ!");
    } else {
      System.out.println("⚠️  НЕКОТОРЫЕ ТЕСТЫ НЕ ПРОЙДЕНЫ");
    }
  }
}

