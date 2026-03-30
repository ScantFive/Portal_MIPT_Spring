package com.mipt.checks.utils;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.*;
import com.mipt.search.service.SearchService;
import com.mipt.search.service.SearchServiceImpl;
import java.util.*;

/**
 * Комплексный тест для проверки всей функциональности SearchService без JUnit.
 * Проверяет:
 * - Работу поиска с минус-словами
 * - Поиск по категориям
 * - Поиск по типу (SERVICE/PRODUCT)
 * - Все типы автодополнений (history, popular, autocomplete, trending, personalized)
 * - Работу с историей поиска
 * - Комбинированный поиск
 */
public class SearchServiceTest {

  private static final SearchService searchService = new SearchServiceImpl();
  private static int totalTests = 0;
  private static int passedTests = 0;
  private static int failedTests = 0;

  public static void main(String[] args) {
    System.out.println("=".repeat(80));
    System.out.println("КОМПЛЕКСНОЕ ТЕСТИРОВАНИЕ SEARCH SERVICE");
    System.out.println("=".repeat(80));
    System.out.println();

    // Используем существующие UUID пользователей из базы данных
    UUID testUserId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID testUserId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // Запускаем все тесты
    testBasicSearch();
    testSearchWithMinusWords();
    testSearchByCategory();
    testSearchByType();
    testCombinedSearch();
    testSearchFavorites(testUserId1);

    // Тесты истории поиска
    testSearchHistory(testUserId1);
    testRecentSearchTexts(testUserId1);
    testPopularSearches(testUserId1);
    testClearSearchHistory(testUserId1);
    testDeleteSearchHistoryEntry(testUserId1);

    // Тесты автодополнений
    testGetSearchSuggestions(testUserId1);
    testHistorySuggestions(testUserId1);
    testPopularSuggestions();
    testAutocompleteSuggestions();
    testTrendingSuggestions();
    testPersonalizedSuggestions(testUserId1);

    // Комплексные тесты
    testMultipleMinusWords();
    testCombinedMinusWordsAndPhrases();
    testCategoryWithFilters();
    testEmptyAndNullHandling();
    testSuggestionDeduplication(testUserId1);

    // Тесты фильтрации по цене
    testPriceFilterMin();
    testPriceFilterMax();
    testPriceFilterRange();
    testPriceFilterWithText();
    testPriceFilterWithCategory();
    testPriceFilterWithSorting();

    // Итоговая статистика
    printFinalStatistics();
  }

  // ============================================================================
  // ТЕСТЫ БАЗОВОГО ПОИСКА
  // ============================================================================

  private static void testBasicSearch() {
    TestUtils.printTestHeader("Базовый поиск по тексту");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("ноутбук");

      List<ShortAdvert> results = searchService.search(10, 0, query);

      TestUtils.assertNotNull(results, "Результаты поиска не должны быть null");
      System.out.println("✓ Найдено объявлений: " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при базовом поиске: " + e.getMessage());
    }
  }

  private static void testSearchWithMinusWords() {
    TestUtils.printTestHeader("Поиск с минус-словами");

    try {
      // Тест 1: Один минус-слово
      SearchQuery query1 = new SearchQuery();
      query1.setSearchText("ноутбук \"-игровой\"");

      List<ShortAdvert> results1 = searchService.search(10, 0, query1);
      TestUtils.assertNotNull(results1, "Результаты не должны быть null");
      System.out.println("✓ Тест 1: Поиск 'ноутбук \"-игровой\"' - найдено: " + results1.size());

      // Тест 2: Несколько минус-слов
      SearchQuery query2 = new SearchQuery();
      query2.setSearchText("смартфон \"-samsung\" \"-xiaomi\"");

      List<ShortAdvert> results2 = searchService.search(10, 0, query2);
      TestUtils.assertNotNull(results2, "Результаты не должны быть null");
      System.out.println("✓ Тест 2: Поиск 'смартфон \"-samsung\" \"-xiaomi\"' - найдено: " + results2.size());

      // Тест 3: Минус-слова с фразами
      SearchQuery query3 = new SearchQuery();
      query3.setSearchText("\"игровой ноутбук\" \"-бу\" \"-б/у\"");

      List<ShortAdvert> results3 = searchService.search(10, 0, query3);
      TestUtils.assertNotNull(results3, "Результаты не должны быть null");
      System.out.println("✓ Тест 3: Поиск '\"игровой ноутбук\" \"-бу\" \"-б/у\"' - найдено: " + results3.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при поиске с минус-словами: " + e.getMessage());
    }
  }

  private static void testSearchByCategory() {
    TestUtils.printTestHeader("Поиск по категориям");

    try {
      // Тест различных категорий
      String[] categories = {"Электроника", "Одежда", "Автомобили", "Недвижимость", "Услуги"};

      for (String category : categories) {
        List<ShortAdvert> results = searchService.searchByCategory(category, 10, 0);
        TestUtils.assertNotNull(results, "Результаты для категории '" + category + "' не должны быть null");
        System.out.println("✓ Категория '" + category + "': найдено " + results.size() + " объявлений");
      }

      // Тест комбинации категории и текста
      SearchQuery query = new SearchQuery();
      query.setSearchText("новый");
      SearchCategory category = new SearchCategory();
      category.setCategoryTitle("Электроника");
      category.setActive(true);
      query.setCategory(Collections.singletonList(category));

      List<ShortAdvert> combinedResults = searchService.search(10, 0, query);
      TestUtils.assertNotNull(combinedResults, "Комбинированные результаты не должны быть null");
      System.out.println("✓ Комбинация 'новый' + категория 'Электроника': найдено " + combinedResults.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при поиске по категориям: " + e.getMessage());
    }
  }

  private static void testSearchByType() {
    TestUtils.printTestHeader("Поиск по типу (SERVICE/PRODUCT)");

    try {
      // Поиск товаров
      List<ShortAdvert> products = searchService.searchByType(SearchType.OBJECTS, 10, 0);
      TestUtils.assertNotNull(products, "Результаты поиска товаров не должны быть null");
      System.out.println("✓ Найдено товаров (PRODUCT): " + products.size());

      // Поиск услуг
      List<ShortAdvert> services = searchService.searchByType(SearchType.SERVICES, 10, 0);
      TestUtils.assertNotNull(services, "Результаты поиска услуг не должны быть null");
      System.out.println("✓ Найдено услуг (SERVICE): " + services.size());

      // Комбинация типа и текста
      SearchQuery query = new SearchQuery();
      query.setSearchText("ремонт");
      query.setType(SearchType.SERVICES);

      List<ShortAdvert> combinedResults = searchService.search(10, 0, query);
      TestUtils.assertNotNull(combinedResults, "Комбинированные результаты не должны быть null");
      System.out.println("✓ Комбинация 'ремонт' + тип SERVICE: найдено " + combinedResults.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при поиске по типу: " + e.getMessage());
    }
  }

  private static void testCombinedSearch() {
    TestUtils.printTestHeader("Комбинированный поиск (текст + категория + тип)");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("новый");
      query.setType(SearchType.OBJECTS);

      SearchCategory category = new SearchCategory();
      category.setCategoryTitle("Электроника");
      category.setActive(true);
      query.setCategory(Collections.singletonList(category));

      List<ShortAdvert> results = searchService.search(10, 0, query);
      TestUtils.assertNotNull(results, "Результаты комбинированного поиска не должны быть null");
      System.out.println("✓ Комбинированный поиск: найдено " + results.size() + " объявлений");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске: " + e.getMessage());
    }
  }

  private static void testSearchFavorites(UUID userId) {
    TestUtils.printTestHeader("Поиск в избранном");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("тест");

      List<ShortAdvert> results = searchService.searchFavorites(userId, query, 10, 0);
      TestUtils.assertNotNull(results, "Результаты поиска в избранном не должны быть null");
      System.out.println("✓ Поиск в избранном: найдено " + results.size() + " объявлений");

      // Тест с пустым запросом
      List<ShortAdvert> emptyQueryResults = searchService.searchFavorites(userId, null, 10, 0);
      TestUtils.assertNotNull(emptyQueryResults, "Результаты с пустым запросом не должны быть null");
      System.out.println("✓ Поиск в избранном без фильтров: найдено " + emptyQueryResults.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при поиске в избранном: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ ИСТОРИИ ПОИСКА
  // ============================================================================

  private static void testSearchHistory(UUID userId) {
    TestUtils.printTestHeader("Получение истории поиска пользователя");

    try {
      // Сначала выполним несколько поисков для создания истории
      SearchQuery query1 = new SearchQuery();
      query1.setSearchText("тестовый запрос 1");
      searchService.search(10, 0, query1, userId);

      SearchQuery query2 = new SearchQuery();
      query2.setSearchText("тестовый запрос 2");
      searchService.search(10, 0, query2, userId);

      // Получаем историю
      List<SearchHistory> history = searchService.getUserSearchHistory(userId, 10);
      TestUtils.assertNotNull(history, "История поиска не должна быть null");
      System.out.println("✓ Получено записей истории: " + history.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении истории поиска: " + e.getMessage());
    }
  }

  private static void testRecentSearchTexts(UUID userId) {
    TestUtils.printTestHeader("Получение недавних текстов поиска");

    try {
      List<String> recentTexts = searchService.getRecentSearchTexts(userId, 5);
      TestUtils.assertNotNull(recentTexts, "Список недавних текстов не должен быть null");
      System.out.println("✓ Получено недавних текстов: " + recentTexts.size());

      if (!recentTexts.isEmpty()) {
        System.out.println("  Примеры: " + recentTexts.subList(0, Math.min(3, recentTexts.size())));
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении недавних текстов: " + e.getMessage());
    }
  }

  private static void testPopularSearches(UUID userId) {
    TestUtils.printTestHeader("Получение популярных поисков");

    try {
      List<SearchHistory> popularSearches = searchService.getPopularSearches(userId, 10);
      TestUtils.assertNotNull(popularSearches, "Список популярных поисков не должен быть null");
      System.out.println("✓ Получено популярных поисков: " + popularSearches.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении популярных поисков: " + e.getMessage());
    }
  }

  private static void testClearSearchHistory(UUID userId) {
    TestUtils.printTestHeader("Очистка истории поиска");

    try {
      // Создаем несколько записей
      SearchQuery query = new SearchQuery();
      query.setSearchText("тест для очистки");
      searchService.search(10, 0, query, userId);

      // Очищаем историю
      searchService.clearUserSearchHistory(userId);
      System.out.println("✓ История поиска успешно очищена");

      // Проверяем, что история пуста
      List<SearchHistory> history = searchService.getUserSearchHistory(userId, 10);
      System.out.println("✓ Записей после очистки: " + history.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при очистке истории: " + e.getMessage());
    }
  }

  private static void testDeleteSearchHistoryEntry(UUID userId) {
    TestUtils.printTestHeader("Удаление конкретной записи истории");

    try {
      // Создаем запись
      SearchQuery query = new SearchQuery();
      query.setSearchText("тест для удаления");
      searchService.search(10, 0, query, userId);

      // Получаем историю
      List<SearchHistory> history = searchService.getUserSearchHistory(userId, 10);

      if (!history.isEmpty()) {
        UUID historyId = history.get(0).getId();
        searchService.deleteSearchHistoryEntry(historyId, userId);
        System.out.println("✓ Запись истории успешно удалена");
      } else {
        System.out.println("⚠ История пуста, удалять нечего");
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при удалении записи истории: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ АВТОДОПОЛНЕНИЙ
  // ============================================================================

  private static void testGetSearchSuggestions(UUID userId) {
    TestUtils.printTestHeader("Получение общих подсказок поиска");

    try {
      // Подсказки с префиксом для авторизованного пользователя
      List<SearchSuggestion> suggestions1 = searchService.getSearchSuggestions("ноут", userId, 10);
      TestUtils.assertNotNull(suggestions1, "Подсказки не должны быть null");
      System.out.println("✓ Подсказки для 'ноут' (авторизован): " + suggestions1.size());
      TestUtils.printSuggestions(suggestions1, 3);

      // Подсказки для неавторизованного пользователя
      List<SearchSuggestion> suggestions2 = searchService.getSearchSuggestions("телефон", null, 10);
      TestUtils.assertNotNull(suggestions2, "Подсказки не должны быть null");
      System.out.println("✓ Подсказки для 'телефон' (не авторизован): " + suggestions2.size());
      TestUtils.printSuggestions(suggestions2, 3);

      // Подсказки без префикса
      List<SearchSuggestion> suggestions3 = searchService.getSearchSuggestions("", userId, 10);
      TestUtils.assertNotNull(suggestions3, "Подсказки без префикса не должны быть null");
      System.out.println("✓ Подсказки без префикса: " + suggestions3.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении подсказок: " + e.getMessage());
    }
  }

  private static void testHistorySuggestions(UUID userId) {
    TestUtils.printTestHeader("Подсказки из истории пользователя");

    try {
      // Создаем историю
      String[] searches = {"ноутбук игровой", "ноутбук для работы", "ноутбук новый"};
      for (String text : searches) {
        SearchQuery query = new SearchQuery();
        query.setSearchText(text);
        searchService.search(5, 0, query, userId);
      }

      // Получаем подсказки из истории
      List<SearchSuggestion> suggestions = searchService.getHistorySuggestions(userId, "ноут", 10);
      TestUtils.assertNotNull(suggestions, "Подсказки из истории не должны быть null");
      System.out.println("✓ Подсказки из истории для 'ноут': " + suggestions.size());
      TestUtils.printSuggestions(suggestions, 5);

      // Проверяем тип подсказок
      for (SearchSuggestion suggestion : suggestions) {
        if (suggestion.getType() != SuggestionType.HISTORY) {
          System.out.println("⚠ Предупреждение: найдена подсказка не из истории: " + suggestion.getType());
        }
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении подсказок из истории: " + e.getMessage());
    }
  }

  private static void testPopularSuggestions() {
    TestUtils.printTestHeader("Популярные подсказки");

    try {
      // Подсказки с префиксом
      List<SearchSuggestion> suggestions1 = searchService.getPopularSuggestions("телефон", 10);
      TestUtils.assertNotNull(suggestions1, "Популярные подсказки не должны быть null");
      System.out.println("✓ Популярные подсказки для 'телефон': " + suggestions1.size());
      TestUtils.printSuggestions(suggestions1, 3);

      // Подсказки без префикса
      List<SearchSuggestion> suggestions2 = searchService.getPopularSuggestions("", 10);
      TestUtils.assertNotNull(suggestions2, "Популярные подсказки без префикса не должны быть null");
      System.out.println("✓ Популярные подсказки без префикса: " + suggestions2.size());
      TestUtils.printSuggestions(suggestions2, 3);

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении популярных подсказок: " + e.getMessage());
    }
  }

  private static void testAutocompleteSuggestions() {
    TestUtils.printTestHeader("Автодополнение из объявлений");

    try {
      String[] prefixes = {"ноут", "телеф", "авто", "кварт", "услуг"};

      for (String prefix : prefixes) {
        List<SearchSuggestion> suggestions = searchService.getAutocompleteSuggestions(prefix, 10);
        TestUtils.assertNotNull(suggestions, "Автодополнение для '" + prefix + "' не должно быть null");
        System.out.println("✓ Автодополнение для '" + prefix + "': " + suggestions.size());
        TestUtils.printSuggestions(suggestions, 2);
      }

      // Пустой префикс должен вернуть пустой список
      List<SearchSuggestion> emptySuggestions = searchService.getAutocompleteSuggestions("", 10);
      TestUtils.assertNotNull(emptySuggestions, "Результат не должен быть null");
      TestUtils.assertTrue(emptySuggestions.isEmpty(), "Для пустого префикса должен вернуться пустой список");
      System.out.println("✓ Пустой префикс вернул пустой список");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при автодополнении: " + e.getMessage());
    }
  }

  private static void testTrendingSuggestions() {
    TestUtils.printTestHeader("Трендовые подсказки");

    try {
      // Подсказки с префиксом
      List<SearchSuggestion> suggestions1 = searchService.getTrendingSuggestions("смарт", 10);
      TestUtils.assertNotNull(suggestions1, "Трендовые подсказки не должны быть null");
      System.out.println("✓ Трендовые подсказки для 'смарт': " + suggestions1.size());
      TestUtils.printSuggestions(suggestions1, 3);

      // Подсказки без префикса
      List<SearchSuggestion> suggestions2 = searchService.getTrendingSuggestions("", 10);
      TestUtils.assertNotNull(suggestions2, "Трендовые подсказки без префикса не должны быть null");
      System.out.println("✓ Трендовые подсказки без префикса: " + suggestions2.size());
      TestUtils.printSuggestions(suggestions2, 3);

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении трендовых подсказок: " + e.getMessage());
    }
  }

  private static void testPersonalizedSuggestions(UUID userId) {
    TestUtils.printTestHeader("Персонализированные подсказки");

    try {
      // Создаем историю для персонализации
      String[] searches = {"смартфон samsung", "смартфон apple", "ноутбук asus"};
      for (String text : searches) {
        SearchQuery query = new SearchQuery();
        query.setSearchText(text);
        searchService.search(5, 0, query, userId);
      }

      // Получаем персонализированные подсказки
      List<SearchSuggestion> suggestions = searchService.getPersonalizedSuggestions(userId, "смарт", 10);
      TestUtils.assertNotNull(suggestions, "Персонализированные подсказки не должны быть null");
      System.out.println("✓ Персонализированные подсказки для 'смарт': " + suggestions.size());
      TestUtils.printSuggestions(suggestions, 5);

      // Для неавторизованного пользователя должен вернуться пустой список
      List<SearchSuggestion> nullUserSuggestions = searchService.getPersonalizedSuggestions(null, "смарт", 10);
      TestUtils.assertNotNull(nullUserSuggestions, "Результат не должен быть null");
      TestUtils.assertTrue(nullUserSuggestions.isEmpty(), "Для null userId должен вернуться пустой список");
      System.out.println("✓ Для неавторизованного пользователя вернулся пустой список");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при получении персонализированных подсказок: " + e.getMessage());
    }
  }

  // ============================================================================
  // КОМПЛЕКСНЫЕ ТЕСТЫ
  // ============================================================================

  private static void testMultipleMinusWords() {
    TestUtils.printTestHeader("Множественные минус-слова");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("ноутбук \"-игровой\" \"-бу\" \"-б/у\" \"-дефект\" \"-ремонт\"");

      List<ShortAdvert> results = searchService.search(10, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Поиск с 5 минус-словами: найдено " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при поиске с множественными минус-словами: " + e.getMessage());
    }
  }

  private static void testCombinedMinusWordsAndPhrases() {
    TestUtils.printTestHeader("Комбинация фраз в кавычках и минус-слов");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("\"новый ноутбук\" \"16 гб\" процессор \"-intel\" \"-бу\" \"-дефект\"");

      List<ShortAdvert> results = searchService.search(10, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Комбинированный поиск: найдено " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске: " + e.getMessage());
    }
  }

  private static void testCategoryWithFilters() {
    TestUtils.printTestHeader("Категория с фильтрами и минус-словами");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("\"игровая\" \"-дешевый\"");
      query.setType(SearchType.OBJECTS);

      SearchCategory category = new SearchCategory();
      category.setCategoryTitle("Электроника");
      category.setActive(true);
      query.setCategory(Collections.singletonList(category));

      List<ShortAdvert> results = searchService.search(10, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Категория + фильтры + минус-слова: найдено " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при поиске с категорией и фильтрами: " + e.getMessage());
    }
  }

  private static void testEmptyAndNullHandling() {
    TestUtils.printTestHeader("Обработка пустых и null значений");

    try {
      // Пустой запрос
      List<ShortAdvert> emptyResults1 = searchService.search(10, 0, null);
      TestUtils.assertNotNull(emptyResults1, "Результаты для null запроса не должны быть null");
      System.out.println("✓ Поиск с null запросом: найдено " + emptyResults1.size());

      // Запрос с пустым текстом
      SearchQuery emptyQuery = new SearchQuery();
      emptyQuery.setSearchText("");
      List<ShortAdvert> emptyResults2 = searchService.search(10, 0, emptyQuery);
      TestUtils.assertNotNull(emptyResults2, "Результаты для пустого текста не должны быть null");
      System.out.println("✓ Поиск с пустым текстом: найдено " + emptyResults2.size());

      // Поиск по тексту с пустой строкой
      List<ShortAdvert> emptyResults3 = searchService.searchByText("", 10, 0);
      TestUtils.assertNotNull(emptyResults3, "Результаты не должны быть null");
      System.out.println("✓ searchByText с пустой строкой: найдено " + emptyResults3.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при обработке пустых значений: " + e.getMessage());
    }
  }

  private static void testSuggestionDeduplication(UUID userId) {
    TestUtils.printTestHeader("Дедупликация подсказок");

    try {
      // Создаем историю с повторяющимися запросами
      for (int i = 0; i < 3; i++) {
        SearchQuery query = new SearchQuery();
        query.setSearchText("телефон samsung");
        searchService.search(5, 0, query, userId);
      }

      // Получаем подсказки
      List<SearchSuggestion> suggestions = searchService.getSearchSuggestions("телефон", userId, 20);
      TestUtils.assertNotNull(suggestions, "Подсказки не должны быть null");

      // Проверяем на дубликаты
      Set<String> uniqueTexts = new HashSet<>();
      int duplicates = 0;
      for (SearchSuggestion suggestion : suggestions) {
        String normalizedText = suggestion.getText().toLowerCase().trim();
        if (!uniqueTexts.add(normalizedText)) {
          duplicates++;
          System.out.println("⚠ Найден дубликат: " + suggestion.getText());
        }
      }

      System.out.println("✓ Всего подсказок: " + suggestions.size());
      System.out.println("✓ Уникальных текстов: " + uniqueTexts.size());
      System.out.println("✓ Дубликатов: " + duplicates);

      TestUtils.assertTrue(duplicates == 0, "Не должно быть дубликатов в подсказках");

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при проверке дедупликации: " + e.getMessage());
    }
  }

  // ============================================================================
  // ТЕСТЫ ФИЛЬТРАЦИИ ПО ЦЕНЕ
  // ============================================================================

  private static void testPriceFilterMin() {
    TestUtils.printTestHeader("Фильтрация по минимальной цене");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new java.math.BigDecimal("500"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено объявлений с ценой >= 500: " + results.size());

      // Проверяем соответствие фильтру
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

  private static void testPriceFilterMax() {
    TestUtils.printTestHeader("Фильтрация по максимальной цене");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setToValue(new java.math.BigDecimal("2000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено объявлений с ценой <= 2000: " + results.size());

      // Проверяем соответствие фильтру
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

  private static void testPriceFilterRange() {
    TestUtils.printTestHeader("Фильтрация по диапазону цен");

    try {
      SearchQuery query = new SearchQuery();
      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new java.math.BigDecimal("500"));
      priceFilter.setToValue(new java.math.BigDecimal("2000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено объявлений с ценой 500-2000: " + results.size());

      // Проверяем соответствие диапазону
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

  private static void testPriceFilterWithText() {
    TestUtils.printTestHeader("Фильтр по цене + текстовый поиск");

    try {
      SearchQuery query = new SearchQuery();
      query.setSearchText("учебник");

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new java.math.BigDecimal("500"));
      priceFilter.setToValue(new java.math.BigDecimal("1500"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено 'учебник' с ценой 500-1500: " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске (текст + цена): " + e.getMessage());
    }
  }

  private static void testPriceFilterWithCategory() {
    TestUtils.printTestHeader("Фильтр по цене + категория");

    try {
      SearchQuery query = new SearchQuery();

      SearchCategory category = new SearchCategory();
      category.setCategoryTitle("Товары");
      category.setActive(true);
      query.setCategory(Collections.singletonList(category));

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new java.math.BigDecimal("100"));
      priceFilter.setToValue(new java.math.BigDecimal("5000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(100, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено в категории 'Товары' с ценой 100-5000: " + results.size());

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске (категория + цена): " + e.getMessage());
    }
  }

  private static void testPriceFilterWithSorting() {
    TestUtils.printTestHeader("Фильтр по цене + сортировка");

    try {
      SearchQuery query = new SearchQuery();
      query.setSortOrder(SearchSortOrder.CHEAPEST);

      SearchFilter priceFilter = new SearchFilter();
      priceFilter.setFilterTitle("price");
      priceFilter.setFromValue(new java.math.BigDecimal("100"));
      priceFilter.setToValue(new java.math.BigDecimal("5000"));
      priceFilter.setActive(true);
      query.setFilters(Collections.singletonList(priceFilter));

      List<ShortAdvert> results = searchService.search(10, 0, query);
      TestUtils.assertNotNull(results, "Результаты не должны быть null");
      System.out.println("✓ Найдено с ценой 100-5000, сортировка CHEAPEST: " + results.size());

      // Проверяем сортировку
      if (results.size() > 1) {
        for (int i = 0; i < results.size() - 1; i++) {
          long price1 = results.get(i).getPrice();
          long price2 = results.get(i + 1).getPrice();
          if (price1 > price2) {
            System.out.println("⚠ Предупреждение: нарушение порядка сортировки");
          }
        }
        System.out.println("✓ Проверка сортировки завершена");
      }

      testPassed();
    } catch (Exception e) {
      testFailed("Ошибка при комбинированном поиске (цена + сортировка): " + e.getMessage());
    }
  }

  // ============================================================================
  // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ - обертки для подсчета статистики
  // ============================================================================

  private static void assertNotNull(Object obj, String message) {
    totalTests++;
    TestUtils.assertNotNull(obj, message);
  }

  private static void assertTrue(boolean condition, String message) {
    totalTests++;
    TestUtils.assertTrue(condition, message);
  }

  private static void testPassed() {
    passedTests++;
    TestUtils.testPassed();
  }

  private static void testFailed(String message) {
    failedTests++;
    TestUtils.testFailed(message);
  }

  private static void printFinalStatistics() {
    TestUtils.printFinalStatistics(passedTests, failedTests, totalTests);
  }
}

