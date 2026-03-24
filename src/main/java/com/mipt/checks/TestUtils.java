package com.mipt.checks;

import com.mipt.model.mainpage.ShortAdvert;
import com.mipt.model.search.SearchHistory;
import com.mipt.model.search.SearchSuggestion;

import java.util.List;

/**
 * Утилитарный класс с общими методами для тестирования и проверки функциональности. Содержит методы
 * для форматированного вывода информации, отрисовки заголовков и т.д.
 */
public class TestUtils {

  // ============================================================================
  // КОНСТАНТЫ ДЛЯ ФОРМАТИРОВАНИЯ
  // ============================================================================

  public static final String SEPARATOR = "=".repeat(80);
  public static final String SEPARATOR_DASH = "-".repeat(80);
  public static final String SEPARATOR_LINE = "─".repeat(80);

  // ============================================================================
  // МЕТОДЫ ДЛЯ ВЫВОДА ЗАГОЛОВКОВ И РАЗДЕЛИТЕЛЕЙ
  // ============================================================================

  /** Печатает заголовок теста с разделителем из дефисов */
  public static void printTestHeader(String testName) {
    System.out.println("\n" + SEPARATOR_DASH);
    System.out.println(testName);
    System.out.println(SEPARATOR_DASH);
  }

  /** Печатает подвал теста с разделителем из дефисов */
  public static void printTestFooter() {
    System.out.println(SEPARATOR_DASH + "\n");
  }

  /** Печатает заголовок секции с разделителем из знаков равенства */
  public static void printSection(String title) {
    System.out.println("\n" + SEPARATOR);
    System.out.println(title);
    System.out.println(SEPARATOR);
  }

  /** Печатает приветственное сообщение */
  public static void printWelcome(String title) {
    System.out.println(SEPARATOR);
    System.out.println(title);
    System.out.println(SEPARATOR);
    System.out.println();
  }

  /** Печатает приветствие для ManualTestingKit */
  public static void printWelcome() {
    System.out.println(SEPARATOR);
    System.out.println("MANUAL TESTING KIT - Ручное тестирование SearchService");
    System.out.println(SEPARATOR);
    System.out.println();
    System.out.println("Этот класс содержит готовые методы для ручной проверки функциональности.");
    System.out.println("Раскомментируйте нужный блок в main() или настройте customSearchManual()");
    System.out.println();
  }

  // ============================================================================
  // МЕТОДЫ ДЛЯ ВЫВОДА ОБЪЯВЛЕНИЙ
  // ============================================================================

  /**
   * Печатает список объявлений в простом формате
   *
   * @param adverts список объявлений
   * @param limit максимальное количество для вывода
   */
  public static void printAdvertisements(List<ShortAdvert> adverts, int limit) {
    if (adverts == null || adverts.isEmpty()) {
      System.out.println("  (пусто)");
      return;
    }

    int count = Math.min(adverts.size(), limit);
    for (int i = 0; i < count; i++) {
      ShortAdvert ad = adverts.get(i);
      System.out.printf(
          "  [%d] %s - %d руб. %s%n",
          i + 1, ad.getTitle(), ad.getPrice(), ad.isFavorite() ? "⭐" : "");
      if (ad.getDescriptionPreview() != null) {
        System.out.println("      " + ad.getDescriptionPreview());
      }
    }

    if (adverts.size() > limit) {
      System.out.println("  ... и еще " + (adverts.size() - limit) + " объявлений");
    }
  }

  /**
   * Печатает список объявлений с полной информацией
   *
   * @param adverts список объявлений
   * @param maxCount максимальное количество для вывода
   */
  public static void printAdvertisementsDetailed(List<ShortAdvert> adverts, int maxCount) {
    if (adverts == null || adverts.isEmpty()) {
      System.out.println("  (пусто)");
      return;
    }

    int count = Math.min(adverts.size(), maxCount);
    for (int i = 0; i < count; i++) {
      ShortAdvert advert = adverts.get(i);
      System.out.printf(
          "  %d. [%s] %s - %d руб. (Автор: %s)%n",
          i + 1,
          advert.getAdvertId().toString().substring(0, 8),
          advert.getTitle(),
          advert.getPrice(),
          advert.getAuthorId().toString().substring(0, 8));
    }

    if (adverts.size() > maxCount) {
      System.out.println("  ... и еще " + (adverts.size() - maxCount) + " объявлений");
    }
  }

  /** Печатает результаты поиска с идентификаторами */
  public static void printSearchResults(List<ShortAdvert> results) {
    if (results == null || results.isEmpty()) {
      System.out.println("   Ничего не найдено");
      return;
    }

    System.out.println("   Найдено: " + results.size() + " объявлений");
    System.out.println();

    int count = Math.min(10, results.size());
    for (int i = 0; i < count; i++) {
      ShortAdvert ad = results.get(i);
      System.out.printf(
          "   %d. [%s] %s - %d₽ (автор: %s)%n",
          i + 1, ad.getAdvertId(), ad.getTitle(), ad.getPrice(), ad.getAuthorId());
    }

    if (results.size() > 10) {
      System.out.println("   ... и еще " + (results.size() - 10) + " объявлений");
    }
  }

  /** Печатает последовательность цен из списка объявлений */
  public static void printPriceSequence(List<ShortAdvert> adverts, int limit) {
    if (adverts == null || adverts.isEmpty()) {
      System.out.println("  (пусто)");
      return;
    }

    int count = Math.min(adverts.size(), limit);
    for (int i = 0; i < count; i++) {
      ShortAdvert ad = adverts.get(i);
      System.out.printf("  %d. %s - %d руб.%n", i + 1, ad.getTitle(), ad.getPrice());
    }
  }

  // ============================================================================
  // МЕТОДЫ ДЛЯ ВЫВОДА ИСТОРИИ ПОИСКА
  // ============================================================================

  /** Печатает историю поиска */
  public static void printHistory(List<SearchHistory> history) {
    if (history == null || history.isEmpty()) {
      System.out.println("   История пуста");
      return;
    }

    System.out.println("   Записей: " + history.size());
    for (int i = 0; i < Math.min(10, history.size()); i++) {
      SearchHistory h = history.get(i);
      System.out.printf(
          "   %d. '%s' - найдено %d результатов (%s)%n",
          i + 1, h.getSearchText(), h.getResultsCount(), h.getCreatedAt());
    }
  }

  // ============================================================================
  // МЕТОДЫ ДЛЯ ВЫВОДА ПОДСКАЗОК ПОИСКА
  // ============================================================================

  /** Печатает список подсказок поиска */
  public static void printSuggestions(List<SearchSuggestion> suggestions) {
    if (suggestions == null || suggestions.isEmpty()) {
      System.out.println("   Подсказок нет");
      return;
    }

    System.out.println("   Найдено: " + suggestions.size() + " подсказок");
    for (int i = 0; i < Math.min(10, suggestions.size()); i++) {
      SearchSuggestion s = suggestions.get(i);
      System.out.printf(
          "   %d. [%s] %s (релевантность: %.1f)%n",
          i + 1, s.getType(), s.getText(), s.getRelevance() != null ? s.getRelevance() : 0.0);
    }
  }

  /** Печатает список подсказок с ограничением */
  public static void printSuggestions(List<SearchSuggestion> suggestions, int limit) {
    if (suggestions == null || suggestions.isEmpty()) {
      System.out.println("   Подсказок нет");
      return;
    }

    int count = Math.min(limit, suggestions.size());
    for (int i = 0; i < count; i++) {
      SearchSuggestion s = suggestions.get(i);
      System.out.printf("  %d. [%s] %s %n", i + 1, s.getType(), s.getText());
    }
  }

  /** Печатает категорию подсказок с заголовком */
  public static void printSuggestionCategory(String title, List<SearchSuggestion> suggestions) {
    System.out.println("\n" + title);
    System.out.println("-".repeat(title.length()));
    printSuggestions(suggestions);
  }

  // ============================================================================
  // МЕТОДЫ ДЛЯ ВЫВОДА СПИСКОВ
  // ============================================================================

  /** Печатает обычный список строк */
  public static void printList(List<String> items) {
    if (items == null || items.isEmpty()) {
      System.out.println("   Список пуст");
      return;
    }

    System.out.println("   Элементов: " + items.size());
    for (int i = 0; i < Math.min(10, items.size()); i++) {
      System.out.printf("   %d. %s%n", i + 1, items.get(i));
    }
  }

  // ============================================================================
  // МЕТОДЫ ДЛЯ ПРОВЕРОК (ASSERTIONS)
  // ============================================================================

  /**
   * Проверяет, что объект не null
   *
   * @throws AssertionError если объект null
   */
  public static void assertNotNull(Object obj, String message) {
    if (obj == null) {
      throw new AssertionError(message);
    }
  }

  /**
   * Проверяет, что условие истинно
   *
   * @throws AssertionError если условие ложно
   */
  public static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  /**
   * Проверяет, что условие ложно
   *
   * @throws AssertionError если условие истинно
   */
  public static void assertFalse(boolean condition, String message) {
    if (condition) {
      throw new AssertionError(message);
    }
  }

  // ============================================================================
  // МЕТОДЫ ДЛЯ ОТОБРАЖЕНИЯ РЕЗУЛЬТАТОВ ТЕСТОВ
  // ============================================================================

  /** Выводит сообщение об успешном прохождении теста */
  public static void testPassed() {
    System.out.println("✅ ТЕСТ ПРОЙДЕН");
  }

  /** Выводит сообщение о провале теста */
  public static void testFailed(String message) {
    System.out.println("❌ ТЕСТ НЕ ПРОЙДЕН: " + message);
  }

  /** Выводит итоговую статистику тестирования */
  public static void printFinalStatistics(int passedTests, int failedTests, int totalTests) {
    System.out.println();
    System.out.println(SEPARATOR);
    System.out.println("ИТОГОВАЯ СТАТИСТИКА");
    System.out.println(SEPARATOR);
    System.out.println("Всего тестов: " + (passedTests + failedTests));
    System.out.println("Пройдено: " + passedTests + " ✅");
    System.out.println("Не пройдено: " + failedTests + " ❌");
    System.out.println("Проверок (assertions): " + totalTests);

    double successRate =
        (passedTests + failedTests) > 0 ? (passedTests * 100.0) / (passedTests + failedTests) : 0;
    System.out.printf("Процент успеха: %.2f%%%n", successRate);
    System.out.println(SEPARATOR);

    if (failedTests == 0) {
      System.out.println("🎉 ВСЕ ТЕСТЫ УСПЕШНО ПРОЙДЕНЫ!");
    } else {
      System.out.println("⚠️  НЕКОТОРЫЕ ТЕСТЫ НЕ ПРОЙДЕНЫ");
    }
  }
}
