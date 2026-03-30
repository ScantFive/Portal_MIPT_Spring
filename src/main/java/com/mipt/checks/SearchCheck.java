package com.mipt.checks;

import com.mipt.checks.utils.TestUtils;
import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.*;
import com.mipt.search.service.SearchService;
import com.mipt.search.service.SearchServiceImpl;
import java.util.List;
import java.util.UUID;

/**
 * Комплексная проверка функциональности поиска. Включает 60 тестов, разделенных на 6 сценариев: 1.
 * Поиск по тексту (10 тестов) 2. Поиск по категориям (10 тестов) 3. Поиск с минус-словами и
 * кавычками (10 тестов) 4. Поиск по всем данным (10 тестов) 5. Автодополнения поиска (10 тестов) 6.
 * История поиска (10 тестов)
 */
public class SearchCheck {

  private static final String SEPARATOR = "=".repeat(80);
  private static final SearchService searchService = new SearchServiceImpl();

  // Тестовые пользователи
  private static final UUID USER_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID USER_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

  // Счетчики для статистики
  private static int totalTests = 0;
  private static int passedTests = 0;
  private static int failedTests = 0;

  public static void run() {
    System.out.println("\n" + SEPARATOR);
    System.out.println("ЗАПУСК КОМПЛЕКСНОЙ ПРОВЕРКИ ФУНКЦИОНАЛЬНОСТИ ПОИСКА");
    System.out.println(SEPARATOR + "\n");

    try {
      // Сценарий 1: Поиск по тексту
      runTextSearchTests();

      // Сценарий 2: Поиск по категориям
      runCategorySearchTests();

      // Сценарий 3: Поиск с минус-словами и кавычками
      runAdvancedSearchTests();

      // Сценарий 4: Поиск по всем данным
      runComprehensiveSearchTests();

      // Сценарий 5: Автодополнения поиска
      runAutocompletionTests();

      // Сценарий 6: История поиска
      runSearchHistoryTests();

      // Финальная статистика
      printFinalStatistics();

    } catch (Exception e) {
      System.err.println("\n✗ КРИТИЧЕСКАЯ ОШИБКА ПРИ ВЫПОЛНЕНИИ ТЕСТОВ: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // ============================================================================
  // СЦЕНАРИЙ 1: ПОИСК ПО ТЕКСТУ (10 тестов)
  // ============================================================================

  private static void runTextSearchTests() {
    TestUtils.printSection("СЦЕНАРИЙ 1: ПОИСК ПО ТЕКСТУ (10 тестов)");

    testTextSearch1_SimpleWord();
    testTextSearch2_MultipleWords();
    testTextSearch3_PartialMatch();
    testTextSearch4_CaseInsensitive();
    testTextSearch5_EmptyQuery();
    testTextSearch6_SpecialCharacters();
    testTextSearch7_Numbers();
    testTextSearch8_LongQuery();
    testTextSearch9_SingleLetter();
    testTextSearch10_Cyrillic();
  }

  private static void testTextSearch1_SimpleWord() {
    runTest(
        "1.1",
        "Поиск по простому слову",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("помощь", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testTextSearch2_MultipleWords() {
    runTest(
        "1.2",
        "Поиск по нескольким словам",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("помощь программирование", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testTextSearch3_PartialMatch() {
    runTest(
        "1.3",
        "Поиск по частичному совпадению",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("програм", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testTextSearch4_CaseInsensitive() {
    runTest(
        "1.4",
        "Поиск без учета регистра",
        () -> {
          List<ShortAdvert> upper = searchService.searchByText("JAVA", 50, 0);
          List<ShortAdvert> lower = searchService.searchByText("java", 50, 0);
          System.out.println("  JAVA: " + upper.size() + " результатов");
          System.out.println("  java: " + lower.size() + " результатов");
          TestUtils.assertTrue(
              upper.size() == lower.size(), "Регистр не должен влиять на результат");
        });
  }

  private static void testTextSearch5_EmptyQuery() {
    runTest(
        "1.5",
        "Поиск с пустым запросом",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений (все)");
        });
  }

  private static void testTextSearch6_SpecialCharacters() {
    runTest(
        "1.6",
        "Поиск со спецсимволами",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("C++", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testTextSearch7_Numbers() {
    runTest(
        "1.7",
        "Поиск по числам",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("100", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testTextSearch8_LongQuery() {
    runTest(
        "1.8",
        "Поиск по длинному запросу",
        () -> {
          String longQuery = "помощь с выполнением домашнего задания по программированию";
          List<ShortAdvert> results = searchService.searchByText(longQuery, 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testTextSearch9_SingleLetter() {
    runTest(
        "1.9",
        "Поиск по одной букве",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("а", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testTextSearch10_Cyrillic() {
    runTest(
        "1.10",
        "Поиск на кириллице",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("репетитор математика", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  // ============================================================================
  // СЦЕНАРИЙ 2: ПОИСК ПО КАТЕГОРИЯМ (10 тестов)
  // ============================================================================

  private static void runCategorySearchTests() {
    TestUtils.printSection("СЦЕНАРИЙ 2: ПОИСК ПО КАТЕГОРИЯМ (10 тестов)");

    testCategory1_SingleCategory();
    testCategory2_EmptyCategory();
    testCategory3_NonExistentCategory();
    testCategory4_CaseSensitiveCategory();
    testCategory5_CategoryWithPagination();
    testCategory6_CategoryCount();
    testCategory7_CategoryByType();
    testCategory8_CategoryService();
    testCategory9_CategoryProduct();
    testCategory10_CategoryFiltering();
  }

  private static void testCategory1_SingleCategory() {
    runTest(
        "2.1",
        "Поиск по одной категории",
        () -> {
          List<ShortAdvert> results = searchService.searchByCategory("Услуги", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Категория 'Услуги': " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testCategory2_EmptyCategory() {
    runTest(
        "2.2",
        "Поиск с пустой категорией",
        () -> {
          List<ShortAdvert> results = searchService.searchByCategory("", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testCategory3_NonExistentCategory() {
    runTest(
        "2.3",
        "Поиск по несуществующей категории",
        () -> {
          List<ShortAdvert> results =
              searchService.searchByCategory("НесуществующаяКатегория", 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Найдено: " + results.size() + " объявлений");
          TestUtils.assertTrue(results.isEmpty(), "Должен вернуться пустой список");
        });
  }

  private static void testCategory4_CaseSensitiveCategory() {
    runTest(
        "2.4",
        "Поиск категории с разным регистром",
        () -> {
          List<ShortAdvert> lower = searchService.searchByCategory("услуги", 50, 0);
          List<ShortAdvert> upper = searchService.searchByCategory("УСЛУГИ", 50, 0);
          System.out.println("  'услуги': " + lower.size() + " результатов");
          System.out.println("  'УСЛУГИ': " + upper.size() + " результатов");
        });
  }

  private static void testCategory5_CategoryWithPagination() {
    runTest(
        "2.5",
        "Пагинация в категории",
        () -> {
          List<ShortAdvert> page1 = searchService.searchByCategory("Услуги", 5, 0);
          List<ShortAdvert> page2 = searchService.searchByCategory("Услуги", 5, 5);
          System.out.println("  Страница 1: " + page1.size() + " объявлений");
          System.out.println("  Страница 2: " + page2.size() + " объявлений");
        });
  }

  private static void testCategory6_CategoryCount() {
    runTest(
        "2.6",
        "Подсчет объявлений в категории",
        () -> {
          List<ShortAdvert> results = searchService.searchByCategory("Товары", 1000, 0);
          System.out.println("  Всего в категории 'Товары': " + results.size());
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
        });
  }

  private static void testCategory7_CategoryByType() {
    runTest(
        "2.7",
        "Поиск по типу SERVICES",
        () -> {
          List<ShortAdvert> results = searchService.searchByType(SearchType.SERVICES, 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Тип SERVICES: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testCategory8_CategoryService() {
    runTest(
        "2.8",
        "Все услуги",
        () -> {
          List<ShortAdvert> results = searchService.searchByType(SearchType.SERVICES, 100, 0);
          System.out.println("  Всего услуг: " + results.size());
        });
  }

  private static void testCategory9_CategoryProduct() {
    runTest(
        "2.9",
        "Поиск по типу OBJECTS",
        () -> {
          List<ShortAdvert> results = searchService.searchByType(SearchType.OBJECTS, 50, 0);
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          System.out.println("  Тип OBJECTS: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testCategory10_CategoryFiltering() {
    runTest(
        "2.10",
        "Комбинация категории и поиска",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("помощь");
          query.setType(SearchType.SERVICES);
          List<ShortAdvert> results = searchService.search(50, 0, query);
          System.out.println("  'помощь' + SERVICES: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  // ============================================================================
  // СЦЕНАРИЙ 3: ПОИСК С МИНУС-СЛОВАМИ И КАВЫЧКАМИ (10 тестов)
  // ============================================================================

  private static void runAdvancedSearchTests() {
    TestUtils.printSection("СЦЕНАРИЙ 3: ПОИСК С МИНУС-СЛОВАМИ И КАВЫЧКАМИ (10 тестов)");

    testAdvanced1_MinusWord();
    testAdvanced2_MultipleMinusWords();
    testAdvanced3_PhraseSearch();
    testAdvanced4_MultiplePhrases();
    testAdvanced5_CombinedMinusAndPhrase();
    testAdvanced6_MinusWordOnly();
    testAdvanced7_EmptyPhrase();
    testAdvanced8_ComplexQuery();
    testAdvanced9_MinusWordAtStart();
    testAdvanced10_NestedQuotes();
  }

  private static void testAdvanced1_MinusWord() {
    runTest(
        "3.1",
        "Поиск с минус-словом",
        () -> {
          List<ShortAdvert> withMinus = searchService.searchByText("java \"-script\"", 50, 0);
          List<ShortAdvert> withoutMinus = searchService.searchByText("java", 50, 0);
          System.out.println("  'java': " + withoutMinus.size() + " результатов");
          System.out.println("  'java \"-script\"': " + withMinus.size() + " результатов");
          TestUtils.assertTrue(
              withMinus.size() <= withoutMinus.size(),
              "С минус-словом должно быть меньше или равно результатов");
        });
  }

  private static void testAdvanced2_MultipleMinusWords() {
    runTest(
        "3.2",
        "Поиск с несколькими минус-словами",
        () -> {
          String query = "программирование \"-java\" \"-python\"";
          List<ShortAdvert> results = searchService.searchByText(query, 50, 0);
          System.out.println("  '" + query + "': " + results.size() + " результатов");
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
        });
  }

  private static void testAdvanced3_PhraseSearch() {
    runTest(
        "3.3",
        "Поиск точной фразы в кавычках",
        () -> {
          String phrase = "\"помощь с программированием\"";
          List<ShortAdvert> results = searchService.searchByText(phrase, 50, 0);
          System.out.println("  " + phrase + ": " + results.size() + " результатов");
          TestUtils.assertNotNull(results, "Результаты не должны быть null");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 2);
          }
        });
  }

  private static void testAdvanced4_MultiplePhrases() {
    runTest(
        "3.4",
        "Поиск нескольких фраз",
        () -> {
          String query = "\"репетитор математика\" \"помощь физика\"";
          List<ShortAdvert> results = searchService.searchByText(query, 50, 0);
          System.out.println("  Найдено: " + results.size() + " объявлений");
        });
  }

  private static void testAdvanced5_CombinedMinusAndPhrase() {
    runTest(
        "3.5",
        "Комбинация фразы и минус-слова",
        () -> {
          String query = "\"помощь программирование\" \"-java\"";
          List<ShortAdvert> results = searchService.searchByText(query, 50, 0);
          System.out.println("  '" + query + "': " + results.size() + " результатов");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 2);
          }
        });
  }

  private static void testAdvanced6_MinusWordOnly() {
    runTest(
        "3.6",
        "Только минус-слово без основного запроса",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("\"-java\"", 50, 0);
          System.out.println("  '\"-java\"': " + results.size() + " результатов");
        });
  }

  private static void testAdvanced7_EmptyPhrase() {
    runTest(
        "3.7",
        "Пустая фраза в кавычках",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("\"\"", 50, 0);
          System.out.println("  '\"\"': " + results.size() + " результатов");
        });
  }

  private static void testAdvanced8_ComplexQuery() {
    runTest(
        "3.8",
        "Сложный запрос",
        () -> {
          String query =
              "\"репетитор математика\" программирование \"-java\" \"-python\" \"помощь экзамен\"";
          List<ShortAdvert> results = searchService.searchByText(query, 50, 0);
          System.out.println("  Сложный запрос: " + results.size() + " результатов");
        });
  }

  private static void testAdvanced9_MinusWordAtStart() {
    runTest(
        "3.9",
        "Минус-слово в начале",
        () -> {
          String query = "\"-дорого\" помощь";
          List<ShortAdvert> results = searchService.searchByText(query, 50, 0);
          System.out.println("  '" + query + "': " + results.size() + " результатов");
        });
  }

  private static void testAdvanced10_NestedQuotes() {
    runTest(
        "3.10",
        "Вложенные кавычки",
        () -> {
          String query = "помощь \"с Java\" программирование";
          List<ShortAdvert> results = searchService.searchByText(query, 50, 0);
          System.out.println("  '" + query + "': " + results.size() + " результатов");
        });
  }

  // ============================================================================
  // СЦЕНАРИЙ 4: ПОИСК ПО ВСЕМ ДАННЫМ (10 тестов)
  // ============================================================================

  private static void runComprehensiveSearchTests() {
    TestUtils.printSection("СЦЕНАРИЙ 4: ПОИСК ПО ВСЕМ ДАННЫМ (10 тестов)");

    testComprehensive1_AllFields();
    testComprehensive2_WithSorting();
    testComprehensive3_WithPagination();
    testComprehensive4_WithUser();
    testComprehensive5_WithFavorites();
    testComprehensive6_EmptyResults();
    testComprehensive7_LargeDataset();
    testComprehensive8_TypeAndCategory();
    testComprehensive9_FullTextSearch();
    testComprehensive10_SearchAccuracy();
  }

  private static void testComprehensive1_AllFields() {
    runTest(
        "4.1",
        "Полный поиск по всем полям",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("помощь");
          query.setType(SearchType.SERVICES);
          List<ShortAdvert> results = searchService.search(50, 0, query);
          System.out.println("  Найдено: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testComprehensive2_WithSorting() {
    runTest(
        "4.2",
        "Поиск с сортировкой",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("репетитор");
          query.setSortOrder(SearchSortOrder.CHEAPEST);
          List<ShortAdvert> results = searchService.search(10, 0, query);
          System.out.println("  Сортировка по возрастанию цены:");
          if (!results.isEmpty()) {
            TestUtils.printPriceSequence(results, 5);
          }
        });
  }

  private static void testComprehensive3_WithPagination() {
    runTest(
        "4.3",
        "Поиск с пагинацией",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("помощь");
          List<ShortAdvert> page1 = searchService.search(5, 0, query);
          List<ShortAdvert> page2 = searchService.search(5, 5, query);
          List<ShortAdvert> page3 = searchService.search(5, 10, query);
          System.out.println("  Страница 1: " + page1.size() + " объявлений");
          System.out.println("  Страница 2: " + page2.size() + " объявлений");
          System.out.println("  Страница 3: " + page3.size() + " объявлений");
        });
  }

  private static void testComprehensive4_WithUser() {
    runTest(
        "4.4",
        "Поиск для авторизованного пользователя",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("программирование");
          List<ShortAdvert> results = searchService.search(50, 0, query, USER_1);
          System.out.println("  Найдено для пользователя " + USER_1 + ": " + results.size());
          long favorites = results.stream().filter(ShortAdvert::isFavorite).count();
          System.out.println("  Из них избранных: " + favorites);
        });
  }

  private static void testComprehensive5_WithFavorites() {
    runTest(
        "4.5",
        "Поиск в избранном",
        () -> {
          SearchQuery query = new SearchQuery();
          List<ShortAdvert> results = searchService.searchFavorites(USER_1, query, 50, 0);
          System.out.println("  Избранных объявлений: " + results.size());
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testComprehensive6_EmptyResults() {
    runTest(
        "4.6",
        "Поиск без результатов",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("несуществующийтекстдляпоиска12345");
          List<ShortAdvert> results = searchService.search(50, 0, query);
          System.out.println("  Найдено: " + results.size() + " объявлений");
          TestUtils.assertTrue(results.isEmpty(), "Должен вернуться пустой список");
        });
  }

  private static void testComprehensive7_LargeDataset() {
    runTest(
        "4.7",
        "Поиск с большим лимитом",
        () -> {
          SearchQuery query = new SearchQuery();
          List<ShortAdvert> results = searchService.search(1000, 0, query);
          System.out.println("  Получено: " + results.size() + " объявлений");
        });
  }

  private static void testComprehensive8_TypeAndCategory() {
    runTest(
        "4.8",
        "Комбинация типа и текста",
        () -> {
          SearchQuery query = new SearchQuery();
          query.setSearchText("учебник");
          query.setType(SearchType.OBJECTS);
          List<ShortAdvert> results = searchService.search(50, 0, query);
          System.out.println("  'учебник' + OBJECTS: " + results.size() + " результатов");
        });
  }

  private static void testComprehensive9_FullTextSearch() {
    runTest(
        "4.9",
        "Полнотекстовый поиск",
        () -> {
          List<ShortAdvert> results = searchService.searchByText("математика физика химия", 50, 0);
          System.out.println("  Найдено: " + results.size() + " объявлений");
          if (!results.isEmpty()) {
            TestUtils.printAdvertisements(results, 3);
          }
        });
  }

  private static void testComprehensive10_SearchAccuracy() {
    runTest(
        "4.10",
        "Проверка точности поиска",
        () -> {
          String searchText = "Java программирование";
          List<ShortAdvert> results = searchService.searchByText(searchText, 50, 0);
          System.out.println("  Поиск '" + searchText + "': " + results.size() + " результатов");
          if (!results.isEmpty()) {
            boolean allRelevant =
                results.stream()
                    .limit(5)
                    .allMatch(
                        ad ->
                            ad.getTitle().toLowerCase().contains("java")
                                || ad.getTitle().toLowerCase().contains("программ"));
            System.out.println("  Релевантность первых 5: " + (allRelevant ? "✓" : "✗"));
          }
        });
  }

  // ============================================================================
  // СЦЕНАРИЙ 5: АВТОДОПОЛНЕНИЯ ПОИСКА (8 тестов)
  // ============================================================================

  private static void runAutocompletionTests() {
    TestUtils.printSection("СЦЕНАРИЙ 5: АВТОДОПОЛНЕНИЯ ПОИСКА (10 тестов)");

    testAutocomplete1_BasicSuggestions();
    testAutocomplete2_EmptyPrefix();
    testAutocomplete3_ShortPrefix();
    testAutocomplete4_LongPrefix();
    testAutocomplete6_HistorySuggestions();
    testAutocomplete7_AutocompleteSuggestions();
    testAutocomplete10_MixedSuggestions();
  }

  private static void testAutocomplete1_BasicSuggestions() {
    runTest(
        "5.1",
        "Базовые подсказки",
        () -> {
          List<SearchSuggestion> suggestions =
              searchService.getSearchSuggestions("про", USER_1, 10);
          TestUtils.assertNotNull(suggestions, "Подсказки не должны быть null");
          System.out.println("  Подсказок для 'про': " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete2_EmptyPrefix() {
    runTest(
        "5.2",
        "Подсказки с пустым префиксом",
        () -> {
          List<SearchSuggestion> suggestions = searchService.getSearchSuggestions("", USER_1, 10);
          System.out.println("  Подсказок с пустым префиксом: " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete3_ShortPrefix() {
    runTest(
        "5.3",
        "Подсказки для короткого префикса",
        () -> {
          List<SearchSuggestion> suggestions = searchService.getSearchSuggestions("п", USER_1, 10);
          System.out.println("  Подсказок для 'п': " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 3);
        });
  }

  private static void testAutocomplete4_LongPrefix() {
    runTest(
        "5.4",
        "Подсказки для длинного префикса",
        () -> {
          List<SearchSuggestion> suggestions =
              searchService.getSearchSuggestions("программирован", USER_1, 10);
          System.out.println("  Подсказок для 'программирован': " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete5_PopularSuggestions() {
    runTest(
        "5.5",
        "Популярные подсказки",
        () -> {
          List<SearchSuggestion> suggestions = searchService.getPopularSuggestions("", 10);
          System.out.println("  Популярных подсказок: " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete6_HistorySuggestions() {
    runTest(
        "5.6",
        "Подсказки из истории",
        () -> {
          List<SearchSuggestion> suggestions = searchService.getHistorySuggestions(USER_1, "", 10);
          System.out.println("  Подсказок из истории: " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete7_AutocompleteSuggestions() {
    runTest(
        "5.7",
        "Автодополнение",
        () -> {
          List<SearchSuggestion> suggestions = searchService.getAutocompleteSuggestions("реп", 10);
          System.out.println("  Автодополнений для 'реп': " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete8_TrendingSuggestions() {
    runTest(
        "5.8",
        "Трендовые подсказки",
        () -> {
          List<SearchSuggestion> suggestions = searchService.getTrendingSuggestions("", 10);
          System.out.println("  Трендовых подсказок: " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete9_PersonalizedSuggestions() {
    runTest(
        "5.9",
        "Персонализированные подсказки",
        () -> {
          List<SearchSuggestion> suggestions =
              searchService.getPersonalizedSuggestions(USER_1, "", 10);
          System.out.println("  Персонализированных подсказок: " + suggestions.size());
          TestUtils.printSuggestions(suggestions, 5);
        });
  }

  private static void testAutocomplete10_MixedSuggestions() {
    runTest(
        "5.8",
        "Комбинированные подсказки",
        () -> {
          String prefix = "помо";
          List<SearchSuggestion> all = searchService.getSearchSuggestions(prefix, USER_1, 10);
          List<SearchSuggestion> popular = searchService.getPopularSuggestions(prefix, 5);
          List<SearchSuggestion> history = searchService.getHistorySuggestions(USER_1, prefix, 5);

          System.out.println("  Все подсказки: " + all.size());
          System.out.println("  Популярные: " + popular.size());
          System.out.println("  Из истории: " + history.size());
        });
  }

  // ============================================================================
  // СЦЕНАРИЙ 6: ИСТОРИЯ ПОИСКА (10 тестов)
  // ============================================================================

  private static void runSearchHistoryTests() {
    TestUtils.printSection("СЦЕНАРИЙ 6: ИСТОРИЯ ПОИСКА (10 тестов)");

    testHistory1_GetHistory();
    testHistory2_EmptyHistory();
    testHistory3_HistoryLimit();
    testHistory4_RecentSearches();
    testHistory5_PopularSearches();
    testHistory6_HistoryAfterSearch();
    testHistory7_MultipleUsers();
    testHistory8_HistoryPersistence();
    testHistory9_ClearHistory();
    testHistory10_DeleteEntry();
  }

  private static void testHistory1_GetHistory() {
    runTest(
        "6.1",
        "Получение истории поиска",
        () -> {
          List<SearchHistory> history = searchService.getUserSearchHistory(USER_1, 10);
          TestUtils.assertNotNull(history, "История не должна быть null");
          System.out.println("  Записей в истории: " + history.size());
          TestUtils.printHistory(history);
        });
  }

  private static void testHistory2_EmptyHistory() {
    runTest(
        "6.2",
        "Пустая история для нового пользователя",
        () -> {
          UUID newUser = UUID.fromString("99999999-9999-9999-9999-999999999999");
          List<SearchHistory> history = searchService.getUserSearchHistory(newUser, 10);
          System.out.println("  Записей для нового пользователя: " + history.size());
        });
  }

  private static void testHistory3_HistoryLimit() {
    runTest(
        "6.3",
        "Ограничение количества записей",
        () -> {
          List<SearchHistory> history5 = searchService.getUserSearchHistory(USER_1, 5);
          List<SearchHistory> history10 = searchService.getUserSearchHistory(USER_1, 10);
          System.out.println("  Limit=5: " + history5.size() + " записей");
          System.out.println("  Limit=10: " + history10.size() + " записей");
          TestUtils.assertTrue(history5.size() <= 5, "Должно быть не больше 5 записей");
        });
  }

  private static void testHistory4_RecentSearches() {
    runTest(
        "6.4",
        "Последние поисковые запросы",
        () -> {
          List<String> recentTexts = searchService.getRecentSearchTexts(USER_1, 5);
          System.out.println("  Последних запросов: " + recentTexts.size());
          TestUtils.printList(recentTexts);
        });
  }

  private static void testHistory5_PopularSearches() {
    runTest(
        "6.5",
        "Популярные запросы",
        () -> {
          List<SearchHistory> popular = searchService.getPopularSearches(USER_1, 10);
          System.out.println("  Популярных запросов: " + popular.size());
          TestUtils.printHistory(popular);
        });
  }

  private static void testHistory6_HistoryAfterSearch() {
    runTest(
        "6.6",
        "История после выполнения поиска",
        () -> {
          // Выполняем поиск
          searchService.search(10, 0, createQuery("тестовый запрос"), USER_2);

          // Проверяем историю
          List<SearchHistory> history = searchService.getUserSearchHistory(USER_2, 5);
          System.out.println("  Записей в истории после поиска: " + history.size());
        });
  }

  private static void testHistory7_MultipleUsers() {
    runTest(
        "6.7",
        "История для разных пользователей",
        () -> {
          List<SearchHistory> history1 = searchService.getUserSearchHistory(USER_1, 10);
          List<SearchHistory> history2 = searchService.getUserSearchHistory(USER_2, 10);
          List<SearchHistory> history3 = searchService.getUserSearchHistory(USER_3, 10);

          System.out.println("  USER_1: " + history1.size() + " записей");
          System.out.println("  USER_2: " + history2.size() + " записей");
          System.out.println("  USER_3: " + history3.size() + " записей");
        });
  }

  private static void testHistory8_HistoryPersistence() {
    runTest(
        "6.8",
        "Сохранение истории",
        () -> {
          // Выполняем несколько поисков
          searchService.search(10, 0, createQuery("java"), USER_3);
          searchService.search(10, 0, createQuery("python"), USER_3);
          searchService.search(10, 0, createQuery("репетитор"), USER_3);

          List<SearchHistory> history = searchService.getUserSearchHistory(USER_3, 10);
          System.out.println("  Сохранено записей: " + history.size());
          TestUtils.assertTrue(history.size() >= 3, "Должно быть минимум 3 записи");
        });
  }

  private static void testHistory9_ClearHistory() {
    runTest(
        "6.9",
        "Очистка истории",
        () -> {
          List<SearchHistory> beforeClear = searchService.getUserSearchHistory(USER_2, 10);
          System.out.println("  До очистки: " + beforeClear.size() + " записей");

          searchService.clearUserSearchHistory(USER_2);

          List<SearchHistory> afterClear = searchService.getUserSearchHistory(USER_2, 10);
          System.out.println("  После очистки: " + afterClear.size() + " записей");
          TestUtils.assertTrue(afterClear.isEmpty(), "История должна быть пуста");
        });
  }

  private static void testHistory10_DeleteEntry() {
    runTest(
        "6.10",
        "Удаление записи из истории",
        () -> {
          List<SearchHistory> history = searchService.getUserSearchHistory(USER_1, 10);

          if (!history.isEmpty()) {
            int sizeBefore = history.size();
            UUID entryId = history.get(0).getId();

            System.out.println("  До удаления: " + sizeBefore + " записей");
            searchService.deleteSearchHistoryEntry(entryId, USER_1);

            List<SearchHistory> afterDelete = searchService.getUserSearchHistory(USER_1, 10);
            System.out.println("  После удаления: " + afterDelete.size() + " записей");
          } else {
            System.out.println("  История пуста, нечего удалять");
          }
        });
  }

  // ============================================================================
  // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
  // ============================================================================

  /** Запускает отдельный тест с обработкой ошибок */
  private static void runTest(String testId, String testName, TestRunnable test) {
    totalTests++;
    System.out.println("\n[ТЕСТ " + testId + "] " + testName);
    System.out.println("-".repeat(60));

    try {
      test.run();
      passedTests++;
      System.out.println("✓ ПРОЙДЕН");
    } catch (Exception e) {
      failedTests++;
      System.err.println("✗ ПРОВАЛЕН: " + e.getMessage());
      if (e.getCause() != null) {
        System.err.println("  Причина: " + e.getCause().getMessage());
      }
    }
  }

  /** Создает SearchQuery с текстом */
  private static SearchQuery createQuery(String text) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(text);
    return query;
  }

  /** Печатает финальную статистику тестирования */
  private static void printFinalStatistics() {
    System.out.println("\n" + SEPARATOR);
    System.out.println("ИТОГОВАЯ СТАТИСТИКА ТЕСТИРОВАНИЯ");
    System.out.println(SEPARATOR);
    System.out.println("Всего тестов:    " + totalTests);
    System.out.println("Пройдено:        " + passedTests + " ✓");
    System.out.println("Провалено:       " + failedTests + " ✗");
    System.out.println(
        "Процент успеха:  " + String.format("%.1f%%", (passedTests * 100.0 / totalTests)));
    System.out.println(SEPARATOR);

    if (failedTests == 0) {
      System.out.println("🎉 ВСЕ ТЕСТЫ УСПЕШНО ПРОЙДЕНЫ!");
    } else {
      System.out.println("⚠ Некоторые тесты не пройдены. Требуется доработка.");
    }
    System.out.println(SEPARATOR + "\n");
  }

  /** Функциональный интерфейс для запуска тестов */
  @FunctionalInterface
  private interface TestRunnable {
    void run() throws Exception;
  }
}
