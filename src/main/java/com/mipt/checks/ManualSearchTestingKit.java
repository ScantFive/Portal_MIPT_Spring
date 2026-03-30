package com.mipt.checks;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.*;
import com.mipt.search.service.SearchService;
import com.mipt.search.service.SearchServiceImpl;
import java.util.*;

public class ManualSearchTestingKit {

  // ============================================================================
  // ГОТОВЫЕ КОНСТАНТЫ И ПЕРЕМЕННЫЕ
  // ============================================================================

  /** Тестовые пользователи из базы данных */
  public static final UUID USER_TIMOFEI = UUID.fromString("11111111-1111-1111-1111-111111111111");
  public static final UUID USER_VLADIMIR = UUID.fromString("22222222-2222-2222-2222-222222222222");
  public static final UUID USER_DANIIL = UUID.fromString("33333333-3333-3333-3333-333333333333");
  public static final UUID USER_GLEB = UUID.fromString("44444444-4444-4444-4444-444444444444");
  public static final UUID USER_ANNA = UUID.fromString("55555555-5555-5555-5555-555555555555");
  public static final UUID USER_MAX = UUID.fromString("66666666-6666-6666-6666-666666666666");
  /** Типы объявлений */
  public static final SearchType TYPE_PRODUCT = SearchType.OBJECTS;
  public static final SearchType TYPE_SERVICE = SearchType.SERVICES;
  /** Категории для тестирования */
  public static final String CATEGORY_ELECTRONICS = "Электроника";
  public static final String CATEGORY_CLOTHES = "Одежда";
  public static final String CATEGORY_CARS = "Автомобили";
  public static final String CATEGORY_REALTY = "Недвижимость";
  public static final String CATEGORY_SERVICES = "Услуги";
  public static final String CATEGORY_BOOKS = "Товары/Книги";
  /** Примеры текстов для поиска (существуют в БД) */
  public static final String TEXT_SQL = "SQL";
  public static final String TEXT_TEXTBOOKS = "учебники";
  public static final String TEXT_SCREWDRIVERS = "отвертки";
  public static final String TEXT_PROGRAMMING = "программирование";
  public static final String TEXT_TUTOR = "репетитор";
  /** Примеры поисковых запросов с минус-словами */
  public static final String QUERY_WITH_MINUS = "ноутбук \"-игровой\"";
  public static final String QUERY_MULTIPLE_MINUS =
      "смартфон \"-samsung\" \"-xiaomi\" \"-дешевый\"";
  public static final String QUERY_WITH_PHRASE = "\"игровой ноутбук\"";
  public static final String QUERY_COMBINED = "\"новый ноутбук\" \"16 гб\" \"-intel\" \"-бу\"";
  /** Лимиты для пагинации */
  public static final long LIMIT_DEFAULT = 10;
  public static final long LIMIT_SMALL = 5;
  public static final long LIMIT_LARGE = 50;
  public static final long OFFSET_DEFAULT = 0;
  /** Экземпляр сервиса для тестирования */
  private static final SearchService searchService = new SearchServiceImpl();

  public static void main(String[] args) {
    runSearchTestingCLI();
  }

  public static void runSearchTestingCLI() {
    Scanner input = new Scanner(System.in);
    UUID currentUserId = USER_TIMOFEI; // Пользователь по умолчанию

    printWelcome();
    printHelp();

    label:
    while (true) {
      System.out.print("\nВвод: ");
      String call = input.nextLine().trim();

      switch (call) {
        case "0":
          System.out.println("Выход из программы...");
          break label;

        case "help":
          printHelp();
          break;

        case "1":
          // Простой поиск по тексту
          System.out.print("Введите текст для поиска: ");
          String searchText = input.nextLine();
          try {
            List<ShortAdvert> results = searchByText(searchText, LIMIT_DEFAULT);
            printSearchResults(results);
          } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
          }
          break;

        case "2":
          // Поиск с минус-словами
          System.out.print("Введите запрос с минус-словами (например: ноутбук \"-игровой\"): ");
          String minusQuery = input.nextLine();
          try {
            List<ShortAdvert> minusResults = searchByText(minusQuery, LIMIT_DEFAULT);
            printSearchResults(minusResults);
          } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
          }
          break;

        case "3":
          // Поиск по категории
          System.out.println("Доступные категории:");
          System.out.println("  1) Электроника");
          System.out.println("  2) Одежда");
          System.out.println("  3) Услуги");
          System.out.println("  4) Товары/Книги");
          System.out.print("Введите название категории: ");
          String category = input.nextLine();
          List<ShortAdvert> catResults = searchByCategory(category, LIMIT_DEFAULT);
          printSearchResults(catResults);
          break;

        case "4":
          // Поиск по типу
          System.out.println("Выберите тип:");
          System.out.println("  1) Товары (OBJECTS)");
          System.out.println("  2) Услуги (SERVICES)");
          System.out.print("Введите номер: ");
          String typeChoice = input.nextLine();
          SearchType type = typeChoice.equals("1") ? TYPE_PRODUCT : TYPE_SERVICE;
          List<ShortAdvert> typeResults = searchByType(type, LIMIT_DEFAULT);
          printSearchResults(typeResults);
          break;

        case "5":
          // История поиска
          System.out.println("\n=== История поиска пользователя ===");
          List<SearchHistory> history = getUserHistory(currentUserId, 10);
          printHistory(history);
          break;

        case "6":
          // Популярные запросы
          System.out.println("\n=== Популярные запросы ===");
          List<SearchHistory> popular = getPopularSearches(currentUserId, 10);
          printHistory(popular);
          break;

        case "7":
          // Автодополнения
          System.out.print("Введите префикс для автодополнения: ");
          String prefix = input.nextLine();
          List<SearchSuggestion> suggestions = getSearchSuggestions(prefix, currentUserId, 10);
          printSuggestions(suggestions);
          break;

        case "8":
          // Комплексный поиск
          performComplexSearch(input, currentUserId);
          break;

        case "9":
          // Поиск с фильтром по цене
          System.out.print("Введите текст для поиска (или Enter для пропуска): ");
          String priceSearchText = input.nextLine();
          System.out.print("Минимальная цена (или Enter для пропуска): ");
          String minPriceStr = input.nextLine();
          System.out.print("Максимальная цена (или Enter для пропуска): ");
          String maxPriceStr = input.nextLine();

          try {
            Integer minPrice = minPriceStr.isEmpty() ? null : Integer.parseInt(minPriceStr);
            Integer maxPrice = maxPriceStr.isEmpty() ? null : Integer.parseInt(maxPriceStr);

            List<ShortAdvert> priceResults;
            if (priceSearchText.isEmpty()) {
              priceResults = searchWithPriceFilter(minPrice, maxPrice);
            } else {
              priceResults = searchWithTextAndPrice(priceSearchText, minPrice, maxPrice);
            }
            printSearchResults(priceResults);
          } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
          }
          break;

        case "10":
          // Очистить историю
          System.out.print("Вы уверены? (yes/no): ");
          String confirm = input.nextLine();
          if (confirm.equalsIgnoreCase("yes")) {
            clearHistory(currentUserId);
            System.out.println("История очищена.");
          }
          break;

        case "11":
          // Сменить пользователя
          System.out.println("Доступные пользователи:");
          System.out.println("  1) Тимофей");
          System.out.println("  2) Владимир");
          System.out.println("  3) Даниил");
          System.out.println("  4) Глеб");
          System.out.println("  5) Анна");
          System.out.println("  6) Макс");
          System.out.print("Выберите номер: ");
          String userChoice = input.nextLine();
          currentUserId = getUserById(userChoice);
          System.out.println("Текущий пользователь изменён.");
          break;

        case "12":
          // Получить все объявления
          System.out.print("Количество записей (по умолчанию 10): ");
          String limitStr = input.nextLine();
          long limit = limitStr.isEmpty() ? LIMIT_DEFAULT : Long.parseLong(limitStr);
          List<ShortAdvert> allResults = searchAll(limit);
          printSearchResults(allResults);
          break;

        case "test":
          // Быстрые тестовые запросы
          runQuickTests();
          break;

        default:
          System.out.println("Неизвестная команда. Введите 'help' для справки.");
          break;
      }
    }

    input.close();
  }

  private static void printWelcome() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("         ИНТЕРАКТИВНЫЙ ТЕСТОВЫЙ ИНТЕРФЕЙС ДЛЯ ПОИСКА");
    System.out.println("=".repeat(80));
  }

  private static void printHelp() {
    System.out.println("\n╔════════════════════════════════════════════════════════════════════════════╗");
    System.out.println("║                          ДОСТУПНЫЕ КОМАНДЫ                                 ║");
    System.out.println("╠════════════════════════════════════════════════════════════════════════════╣");
    System.out.println("║  0    | Выйти из программы                                                 ║");
    System.out.println("║  help | Показать эту справку                                               ║");
    System.out.println("║                                                                            ║");
    System.out.println("║  ОСНОВНЫЕ ПОИСКОВЫЕ ФУНКЦИИ:                                               ║");
    System.out.println("║  1    | Простой поиск по тексту                                            ║");
    System.out.println("║  2    | Поиск с минус-словами                                              ║");
    System.out.println("║  3    | Поиск по категории                                                 ║");
    System.out.println("║  4    | Поиск по типу (товары/услуги)                                      ║");
    System.out.println("║  8    | Комплексный поиск (с несколькими параметрами)                      ║");
    System.out.println("║  9    | Поиск с фильтром по цене                                           ║");
    System.out.println("║  12   | Получить все объявления                                            ║");
    System.out.println("║                                                                            ║");
    System.out.println("║  ИСТОРИЯ И ПОДСКАЗКИ:                                                      ║");
    System.out.println("║  5    | Показать историю поиска                                            ║");
    System.out.println("║  6    | Показать популярные запросы                                        ║");
    System.out.println("║  7    | Получить автодополнения                                            ║");
    System.out.println("║  10   | Очистить историю поиска                                            ║");
    System.out.println("║                                                                            ║");
    System.out.println("║  НАСТРОЙКИ:                                                                ║");
    System.out.println("║  11   | Сменить текущего пользователя                                      ║");
    System.out.println("║                                                                            ║");
    System.out.println("║  ТЕСТИРОВАНИЕ:                                                             ║");
    System.out.println("║  test | Запустить быстрые тестовые запросы                                 ║");
    System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
  }

  private static void performComplexSearch(Scanner input, UUID userId) {
    System.out.println("\n=== КОМПЛЕКСНЫЙ ПОИСК ===");

    try {
      System.out.print("Текст поиска (или Enter для пропуска): ");
      String text = input.nextLine();
      if (text.isEmpty()) text = null;

      System.out.print("Тип (1-товары, 2-услуги, Enter для пропуска): ");
      String typeStr = input.nextLine();
      SearchType type = typeStr.equals("1") ? TYPE_PRODUCT :
                        typeStr.equals("2") ? TYPE_SERVICE : null;

      System.out.print("Категория (или Enter для пропуска): ");
      String category = input.nextLine();
      if (category.isEmpty()) category = null;

      System.out.print("Мин. цена (или Enter для пропуска): ");
      String minPriceStr = input.nextLine();
      Integer minPrice = minPriceStr.isEmpty() ? null : Integer.parseInt(minPriceStr);

      System.out.print("Макс. цена (или Enter для пропуска): ");
      String maxPriceStr = input.nextLine();
      Integer maxPrice = maxPriceStr.isEmpty() ? null : Integer.parseInt(maxPriceStr);

      System.out.print("Сортировка (1-новые, 2-старые, 3-дешевые, 4-дорогие, Enter для пропуска): ");
      String sortStr = input.nextLine();
      SearchSortOrder sortOrder = null;
      switch (sortStr) {
        case "1": sortOrder = SearchSortOrder.NEWEST; break;
        case "2": sortOrder = SearchSortOrder.OLDEST; break;
        case "3": sortOrder = SearchSortOrder.CHEAPEST; break;
        case "4": sortOrder = SearchSortOrder.EXPENSIVE; break;
      }

      List<ShortAdvert> results = searchWithAllFilters(text, type, category, minPrice, maxPrice, sortOrder);
      printSearchResults(results);
    } catch (IllegalArgumentException e) {
      System.out.println("❌ Ошибка: " + e.getMessage());
    }
  }

  private static UUID getUserById(String choice) {
    switch (choice) {
      case "1": return USER_TIMOFEI;
      case "2": return USER_VLADIMIR;
      case "3": return USER_DANIIL;
      case "4": return USER_GLEB;
      case "5": return USER_ANNA;
      case "6": return USER_MAX;
      default: return USER_TIMOFEI;
    }
  }

  private static void runQuickTests() {
    System.out.println("\n=== ЗАПУСК БЫСТРЫХ ТЕСТОВ ===");

    try {
      System.out.println("\n1. Поиск 'SQL':");
      List<ShortAdvert> test1 = searchByText(TEXT_SQL, 5);
      printSearchResults(test1);

      System.out.println("\n2. Поиск с минус-словом:");
      List<ShortAdvert> test2 = searchByText(QUERY_WITH_MINUS, 5);
      printSearchResults(test2);

      System.out.println("\n3. Поиск по категории 'Услуги':");
      List<ShortAdvert> test3 = searchByCategory(CATEGORY_SERVICES, 5);
      printSearchResults(test3);

      System.out.println("\nТесты завершены.");
    } catch (IllegalArgumentException e) {
      System.out.println("❌ Ошибка при выполнении теста: " + e.getMessage());
    }
  }

  private static void printSearchResults(List<ShortAdvert> results) {
    if (results == null || results.isEmpty()) {
      System.out.println("Результатов не найдено.");
      return;
    }

    System.out.println("\n┌" + "─".repeat(78) + "┐");
    System.out.println("│ Найдено результатов: " + results.size() + " ".repeat(55 - String.valueOf(results.size()).length()) + "│");
    System.out.println("├" + "─".repeat(78) + "┤");

    for (int i = 0; i < results.size(); i++) {
      ShortAdvert ad = results.get(i);
      System.out.printf("│ %2d. %-72s │%n", i + 1, truncate(ad.getTitle(), 72));
      System.out.printf("│     ID: %-68s │%n", ad.getAdvertId());
      if (ad.getPrice() > 0) {
        System.out.printf("│     Цена: %-66s │%n", ad.getPrice() + " руб.");
      }
      if (i < results.size() - 1) {
        System.out.println("├" + "─".repeat(78) + "┤");
      }
    }

    System.out.println("└" + "─".repeat(78) + "┘");
  }

  private static void printHistory(List<SearchHistory> history) {
    if (history == null || history.isEmpty()) {
      System.out.println("История пуста.");
      return;
    }

    System.out.println("\n┌" + "─".repeat(78) + "┐");
    System.out.println("│ История поиска (" + history.size() + " записей)" + " ".repeat(59 - String.valueOf(history.size()).length()) + "│");
    System.out.println("├" + "─".repeat(78) + "┤");

    for (int i = 0; i < history.size(); i++) {
      SearchHistory h = history.get(i);
      System.out.printf("│ %2d. %-72s │%n", i + 1, truncate(h.getSearchText(), 72));
      System.out.printf("│     Дата: %-68s │%n", h.getCreatedAt());
      if (i < history.size() - 1) {
        System.out.println("├" + "─".repeat(78) + "┤");
      }
    }

    System.out.println("└" + "─".repeat(78) + "┘");
  }

  private static void printSuggestions(List<SearchSuggestion> suggestions) {
    if (suggestions == null || suggestions.isEmpty()) {
      System.out.println("Подсказок не найдено.");
      return;
    }

    System.out.println("\n┌" + "─".repeat(78) + "┐");
    System.out.println("│ Подсказки (" + suggestions.size() + " найдено)" + " ".repeat(62 - String.valueOf(suggestions.size()).length()) + "│");
    System.out.println("├" + "─".repeat(78) + "┤");

    for (int i = 0; i < suggestions.size(); i++) {
      SearchSuggestion s = suggestions.get(i);
      System.out.printf("│ %2d. %-72s │%n", i + 1, truncate(s.getText(), 72));
      if (i < suggestions.size() - 1) {
        System.out.println("├" + "─".repeat(78) + "┤");
      }
    }

    System.out.println("└" + "─".repeat(78) + "┘");
  }

  private static String truncate(String text, int maxLength) {
    if (text == null) return "";
    if (text.length() <= maxLength) return text;
    return text.substring(0, maxLength - 3) + "...";
  }



  // ============================================================================
  // СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ ПОСТРОЕНИЯ ЗАПРОСОВ
  // ============================================================================

  /** Простой поиск по тексту */
  public static List<ShortAdvert> searchByText(String text, long limit) {
    return searchService.searchByText(text, limit, OFFSET_DEFAULT);
  }

  /** Поиск по тексту с offset */
  public static List<ShortAdvert> searchByTextWithOffset(String text, long limit, long offset) {
    return searchService.searchByText(text, limit, offset);
  }

  /** Получить все объявления */
  public static List<ShortAdvert> searchAll(long limit) {
    return searchService.search(limit, OFFSET_DEFAULT, null);
  }

  /** Поиск по категории */
  public static List<ShortAdvert> searchByCategory(String category, long limit) {
    return searchService.searchByCategory(category, limit, OFFSET_DEFAULT);
  }

  /** Поиск по категории и тексту */
  public static List<ShortAdvert> searchByCategoryAndText(
      String category, String text, long limit) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(text);

    SearchCategory cat = new SearchCategory();
    cat.setCategoryTitle(category);
    cat.setActive(true);
    query.setCategory(Collections.singletonList(cat));

    return searchService.search(limit, OFFSET_DEFAULT, query);
  }

  /** Поиск по нескольким категориям */
  public static List<ShortAdvert> searchByMultipleCategories(List<String> categories, long limit) {
    SearchQuery query = new SearchQuery();

    List<SearchCategory> cats = new ArrayList<>();
    for (String categoryTitle : categories) {
      SearchCategory cat = new SearchCategory();
      cat.setCategoryTitle(categoryTitle);
      cat.setActive(true);
      cats.add(cat);
    }
    query.setCategory(cats);

    return searchService.search(limit, OFFSET_DEFAULT, query);
  }

  /** Поиск по типу */
  public static List<ShortAdvert> searchByType(SearchType type, long limit) {
    return searchService.searchByType(type, limit, OFFSET_DEFAULT);
  }

  /** Поиск по типу и тексту */
  public static List<ShortAdvert> searchByTypeAndText(SearchType type, String text, long limit) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(text);
    query.setType(type);
    return searchService.search(limit, OFFSET_DEFAULT, query);
  }

  /** Поиск с полным запросом */
  public static List<ShortAdvert> search(SearchQuery query, long limit, long offset) {
    return searchService.search(limit, offset, query);
  }

  /** Поиск с полным запросом и userId */
  public static List<ShortAdvert> search(SearchQuery query, long limit, long offset, UUID userId) {
    return searchService.search(limit, offset, query, userId);
  }

  /** Построить комплексный запрос */
  public static SearchQuery buildComplexQuery(
      String text, SearchType type, String category, SearchSortOrder sortOrder) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(text);
    query.setType(type);
    query.setSortOrder(sortOrder);

    if (category != null) {
      SearchCategory cat = new SearchCategory();
      cat.setCategoryTitle(category);
      cat.setActive(true);
      query.setCategory(Collections.singletonList(cat));
    }

    return query;
  }

  /** Поиск в избранном */
  public static List<ShortAdvert> searchFavorites(UUID userId, String text, long limit) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(text);
    return searchService.searchFavorites(userId, query, limit, OFFSET_DEFAULT);
  }

  // ============================================================================
  // СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ ФИЛЬТРОВ ПО ЦЕНЕ
  // ============================================================================

  /** Создать фильтр по цене */
  public static SearchFilter createPriceFilter(Integer minPrice, Integer maxPrice) {
    SearchFilter filter = new SearchFilter();
    filter.setFilterTitle("price");
    if (minPrice != null) {
      filter.setFromValue(new java.math.BigDecimal(minPrice));
    }
    if (maxPrice != null) {
      filter.setToValue(new java.math.BigDecimal(maxPrice));
    }
    filter.setActive(true);
    return filter;
  }

  /** Поиск с фильтром по цене */
  public static List<ShortAdvert> searchWithPriceFilter(Integer minPrice, Integer maxPrice) {
    SearchQuery query = new SearchQuery();
    SearchFilter priceFilter = createPriceFilter(minPrice, maxPrice);
    query.setFilters(Collections.singletonList(priceFilter));
    return searchService.search(LIMIT_DEFAULT, OFFSET_DEFAULT, query);
  }

  /** Поиск по тексту с фильтром по цене */
  public static List<ShortAdvert> searchWithTextAndPrice(
      String text, Integer minPrice, Integer maxPrice) {
    SearchQuery query = new SearchQuery();
    query.setSearchText(text);
    SearchFilter priceFilter = createPriceFilter(minPrice, maxPrice);
    query.setFilters(Collections.singletonList(priceFilter));
    return searchService.search(LIMIT_DEFAULT, OFFSET_DEFAULT, query);
  }

  /** Поиск по категории с фильтром по цене */
  public static List<ShortAdvert> searchWithCategoryAndPrice(
      String category, Integer minPrice, Integer maxPrice) {
    SearchQuery query = new SearchQuery();

    SearchCategory cat = new SearchCategory();
    cat.setCategoryTitle(category);
    cat.setActive(true);
    query.setCategory(Collections.singletonList(cat));

    SearchFilter priceFilter = createPriceFilter(minPrice, maxPrice);
    query.setFilters(Collections.singletonList(priceFilter));

    return searchService.search(LIMIT_DEFAULT, OFFSET_DEFAULT, query);
  }

  /** Поиск с фильтром по цене и сортировкой */
  public static List<ShortAdvert> searchWithPriceAndSort(
      Integer minPrice, Integer maxPrice, SearchSortOrder sortOrder) {
    SearchQuery query = new SearchQuery();
    query.setSortOrder(sortOrder);

    SearchFilter priceFilter = createPriceFilter(minPrice, maxPrice);
    query.setFilters(Collections.singletonList(priceFilter));

    return searchService.search(LIMIT_DEFAULT, OFFSET_DEFAULT, query);
  }

  /** Комплексный поиск: текст + категория + тип + цена + сортировка */
  public static List<ShortAdvert> searchWithAllFilters(
      String text,
      SearchType type,
      String category,
      Integer minPrice,
      Integer maxPrice,
      SearchSortOrder sortOrder) {

    SearchQuery query = new SearchQuery();
    query.setSearchText(text);
    query.setType(type);
    query.setSortOrder(sortOrder);

    if (category != null) {
      SearchCategory cat = new SearchCategory();
      cat.setCategoryTitle(category);
      cat.setActive(true);
      query.setCategory(Collections.singletonList(cat));
    }

    if (minPrice != null || maxPrice != null) {
      SearchFilter priceFilter = createPriceFilter(minPrice, maxPrice);
      query.setFilters(Collections.singletonList(priceFilter));
    }

    return searchService.search(LIMIT_DEFAULT, OFFSET_DEFAULT, query);
  }

  // ============================================================================
  // СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ ИСТОРИИ ПОИСКА
  // ============================================================================

  /** Создать историю поиска */
  public static void createSearchHistory(UUID userId, String... searchTexts) {
    for (String text : searchTexts) {
      SearchQuery query = new SearchQuery();
      query.setSearchText(text);
      searchService.search(5, 0, query, userId);
    }
  }

  /** Получить историю пользователя */
  public static List<SearchHistory> getUserHistory(UUID userId, int limit) {
    return searchService.getUserSearchHistory(userId, limit);
  }

  /** Получить недавние тексты поиска */
  public static List<String> getRecentSearchTexts(UUID userId, int limit) {
    return searchService.getRecentSearchTexts(userId, limit);
  }

  /** Получить популярные запросы */
  public static List<SearchHistory> getPopularSearches(UUID userId, int limit) {
    return searchService.getPopularSearches(userId, limit);
  }

  /** Очистить историю */
  public static void clearHistory(UUID userId) {
    searchService.clearUserSearchHistory(userId);
  }

  /** Удалить запись из истории */
  public static void deleteHistoryEntry(UUID historyId, UUID userId) {
    searchService.deleteSearchHistoryEntry(historyId, userId);
  }

  // ============================================================================
  // СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ АВТОДОПОЛНЕНИЙ
  // ============================================================================

  /** Получить все подсказки */
  public static List<SearchSuggestion> getSearchSuggestions(String prefix, UUID userId, int limit) {
    return searchService.getSearchSuggestions(prefix, userId, limit);
  }

  /** Получить подсказки из истории */
  public static List<SearchSuggestion> getHistorySuggestions(
      UUID userId, String prefix, int limit) {
    return searchService.getHistorySuggestions(userId, prefix, limit);
  }

  /** Получить популярные подсказки */
  public static List<SearchSuggestion> getPopularSuggestions(String prefix, int limit) {
    return searchService.getPopularSuggestions(prefix, limit);
  }

  /** Получить автодополнение */
  public static List<SearchSuggestion> getAutocompleteSuggestions(String prefix, int limit) {
    return searchService.getAutocompleteSuggestions(prefix, limit);
  }

  /** Получить трендовые подсказки */
  public static List<SearchSuggestion> getTrendingSuggestions(String prefix, int limit) {
    return searchService.getTrendingSuggestions(prefix, limit);
  }

  /** Получить персонализированные подсказки */
  public static List<SearchSuggestion> getPersonalizedSuggestions(
      UUID userId, String prefix, int limit) {
    return searchService.getPersonalizedSuggestions(userId, prefix, limit);
  }
}
