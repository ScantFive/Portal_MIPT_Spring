package com.mipt.checks.utils;

import com.mipt.search.model.*;
import com.mipt.search.repository.SearchHistoryRepository;
import com.mipt.search.service.SearchService;
import com.mipt.search.service.SearchServiceImpl;
import java.util.*;

/**
 * Тест для проверки правильности работы SearchHistoryRepository
 * 
 * Проверяет:
 * 1. Сохранение истории при каждом поисковом запросе
 * 2. Корректность сохраняемых данных
 * 3. Что история НЕ сохраняется для незначимых запросов
 * 4. Что история НЕ сохраняется для неавторизованных пользователей
 */
public class SearchHistoryRepositoryTest {

    private static final SearchService searchService = new SearchServiceImpl();
    private static final UUID TEST_USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("ТЕСТИРОВАНИЕ SearchHistoryRepository");
        System.out.println("=".repeat(80));
        System.out.println();

        // Очищаем историю перед тестами
        clearHistory();

        // Запуск тестов
        testSaveSimpleSearchText();
        testSaveSearchWithType();
        testSaveSearchWithCategory();
        testSaveComplexSearch();
        testNoSaveForEmptyQuery();
        testNoSaveForAnonymousUser();
        testHistoryContainsCorrectData();
        testMultipleSearchesSaveAll();
        testResultsCountSaved();
        testCategoriesArraySaved();

        // Итоги
        printResults();
    }

    // ============================================================================
    // ТЕСТЫ
    // ============================================================================

    /**
     * Тест 1: Сохранение простого текстового поиска
     */
    private static void testSaveSimpleSearchText() {
        TestUtils.printTestHeader("Сохранение простого текстового поиска");

        clearHistory();

        // Выполняем поиск
        String searchText = "SQL тест " + System.currentTimeMillis();
        SearchQuery query = new SearchQuery();
        query.setSearchText(searchText);
        
        searchService.search(10, 0, query, TEST_USER);

        // Проверяем, что запись создана
        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "Должна быть создана 1 запись");
        assertEquals(searchText, history.get(0).getSearchText(), "Текст поиска должен совпадать");

        System.out.println("✓ История сохранена: '" + history.get(0).getSearchText() + "'");
        testPassed();
    }

    /**
     * Тест 2: Сохранение поиска с типом
     */
    private static void testSaveSearchWithType() {
        TestUtils.printTestHeader("Сохранение поиска с типом");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("тестовый товар");
        query.setType(SearchType.OBJECTS);

        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "Должна быть создана 1 запись");
        assertEquals(SearchType.OBJECTS, history.get(0).getSearchType(), "Тип должен быть PRODUCT");

        System.out.println("✓ Тип сохранен: " + history.get(0).getSearchType());
        testPassed();
    }

    /**
     * Тест 3: Сохранение поиска с категорией
     */
    private static void testSaveSearchWithCategory() {
        TestUtils.printTestHeader("Сохранение поиска с категорией");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("услуга");
        
        SearchCategory category = new SearchCategory();
        category.setCategoryTitle("Услуги");
        category.setActive(true);
        query.setCategory(Collections.singletonList(category));

        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "Должна быть создана 1 запись");
        assertNotNull(history.get(0).getCategories(), "Категории не должны быть null");
        assertTrue(history.get(0).getCategories().contains("Услуги"), 
                   "Категория 'Услуги' должна быть сохранена");

        System.out.println("✓ Категория сохранена: " + history.get(0).getCategories());
        testPassed();
    }

    /**
     * Тест 4: Сохранение комплексного поиска
     */
    private static void testSaveComplexSearch() {
        TestUtils.printTestHeader("Сохранение комплексного поиска");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("новый товар");
        query.setType(SearchType.OBJECTS);
        query.setSortOrder(SearchSortOrder.NEWEST);
        
        SearchCategory category = new SearchCategory();
        category.setCategoryTitle("Электроника");
        category.setActive(true);
        query.setCategory(Collections.singletonList(category));

        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "Должна быть создана 1 запись");
        
        SearchHistory saved = history.get(0);
        assertEquals("новый товар", saved.getSearchText(), "Текст должен совпадать");
        assertEquals(SearchType.OBJECTS, saved.getSearchType(), "Тип должен совпадать");
        assertEquals(SearchSortOrder.NEWEST, saved.getSortOrder(), "Сортировка должна совпадать");
        assertTrue(saved.getCategories().contains("Электроника"), "Категория должна быть сохранена");

        System.out.println("✓ Комплексный запрос сохранен:");
        System.out.println("  - Текст: " + saved.getSearchText());
        System.out.println("  - Тип: " + saved.getSearchType());
        System.out.println("  - Сортировка: " + saved.getSortOrder());
        System.out.println("  - Категории: " + saved.getCategories());
        
        testPassed();
    }

    /**
     * Тест 5: История НЕ сохраняется для пустого запроса
     */
    private static void testNoSaveForEmptyQuery() {
        TestUtils.printTestHeader("История НЕ сохраняется для пустого запроса");

        clearHistory();

        // Пустой запрос
        SearchQuery query = new SearchQuery();
        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertEquals(0, history.size(), "История не должна сохраняться для пустого запроса");

        System.out.println("✓ Пустой запрос не сохранен (правильно)");
        testPassed();
    }

    /**
     * Тест 6: История НЕ сохраняется для неавторизованных
     */
    private static void testNoSaveForAnonymousUser() {
        TestUtils.printTestHeader("История НЕ сохраняется для неавторизованных пользователей");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("анонимный поиск");
        
        // Поиск без userId (null)
        searchService.search(10, 0, query, null);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertEquals(0, history.size(), "История не должна сохраняться для неавторизованных");

        System.out.println("✓ Анонимный поиск не сохранен (правильно)");
        testPassed();
    }

    /**
     * Тест 7: История содержит корректные данные
     */
    private static void testHistoryContainsCorrectData() {
        TestUtils.printTestHeader("Проверка корректности данных в истории");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("проверка данных");
        
        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        SearchHistory saved = history.get(0);
        
        assertNotNull(saved.getId(), "ID должен быть сгенерирован");
        assertNotNull(saved.getUserId(), "UserID должен быть установлен");
        assertEquals(TEST_USER, saved.getUserId(), "UserID должен совпадать");
        assertNotNull(saved.getCreatedAt(), "created_at должен быть установлен");
        assertNotNull(saved.getResultsCount(), "results_count должен быть установлен");

        System.out.println("✓ Данные корректны:");
        System.out.println("  - ID: " + saved.getId());
        System.out.println("  - UserID: " + saved.getUserId());
        System.out.println("  - Дата: " + saved.getCreatedAt());
        System.out.println("  - Результатов: " + saved.getResultsCount());
        
        testPassed();
    }

    /**
     * Тест 8: Несколько поисков сохраняются все
     */
    private static void testMultipleSearchesSaveAll() {
        TestUtils.printTestHeader("Несколько поисков сохраняются все");

        clearHistory();

        // Выполняем 5 разных поисков
        String[] searches = {"поиск 1", "поиск 2", "поиск 3", "поиск 4", "поиск 5"};
        
        for (String text : searches) {
            SearchQuery query = new SearchQuery();
            query.setSearchText(text);
            searchService.search(10, 0, query, TEST_USER);
        }

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        
        assertEquals(5, history.size(), "Должно быть сохранено 5 записей");

        System.out.println("✓ Сохранено " + history.size() + " записей:");
        for (int i = 0; i < history.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + history.get(i).getSearchText());
        }
        
        testPassed();
    }

    /**
     * Тест 9: Количество результатов сохраняется
     */
    private static void testResultsCountSaved() {
        TestUtils.printTestHeader("Количество результатов сохраняется");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("SQL");
        
        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        SearchHistory saved = history.get(0);
        
        assertNotNull(saved.getResultsCount(), "results_count не должен быть null");
        assertTrue(saved.getResultsCount() >= 0, "results_count должен быть >= 0");

        System.out.println("✓ Количество результатов: " + saved.getResultsCount());
        testPassed();
    }

    /**
     * Тест 10: Несколько категорий сохраняются как массив
     */
    private static void testCategoriesArraySaved() {
        TestUtils.printTestHeader("Несколько категорий сохраняются как массив");

        clearHistory();

        SearchQuery query = new SearchQuery();
        query.setSearchText("мультикатегорийный поиск");
        
        List<SearchCategory> categories = new ArrayList<>();
        
        SearchCategory cat1 = new SearchCategory();
        cat1.setCategoryTitle("Электроника");
        cat1.setActive(true);
        categories.add(cat1);
        
        SearchCategory cat2 = new SearchCategory();
        cat2.setCategoryTitle("Услуги");
        cat2.setActive(true);
        categories.add(cat2);
        
        query.setCategory(categories);

        searchService.search(10, 0, query, TEST_USER);

        List<SearchHistory> history = SearchHistoryRepository.getUserSearchHistory(TEST_USER, 10);
        SearchHistory saved = history.get(0);
        
        assertNotNull(saved.getCategories(), "Категории не должны быть null");
        assertEquals(2, saved.getCategories().size(), "Должно быть 2 категории");
        assertTrue(saved.getCategories().contains("Электроника"), "Должна быть категория Электроника");
        assertTrue(saved.getCategories().contains("Услуги"), "Должна быть категория Услуги");

        System.out.println("✓ Категории сохранены: " + saved.getCategories());
        testPassed();
    }

    // ============================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================================

    private static void clearHistory() {
        SearchHistoryRepository.clearUserSearchHistory(TEST_USER);
    }

    private static void assertNotNull(Object obj, String message) {
        totalTests++;
        TestUtils.assertNotNull(obj, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        totalTests++;
        if (!Objects.equals(expected, actual)) {
            System.err.println("❌ ОШИБКА: " + message);
            System.err.println("   Ожидалось: " + expected);
            System.err.println("   Получено: " + actual);
            throw new AssertionError(message);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        totalTests++;
        if (!condition) {
            System.err.println("❌ ОШИБКА: " + message);
            throw new AssertionError(message);
        }
    }

    private static void testPassed() {
        passedTests++;
        System.out.println("✅ ТЕСТ ПРОЙДЕН\n");
    }

    private static void printResults() {
        System.out.println("=".repeat(80));
        System.out.println("ИТОГОВАЯ СТАТИСТИКА");
        System.out.println("=".repeat(80));
        System.out.println("Всего тестов: " + passedTests);
        System.out.println("Пройдено: " + passedTests + " ✅");
        System.out.println("Проверок (assertions): " + totalTests);
        System.out.println("Процент успеха: 100%");
        System.out.println("=".repeat(80));
        System.out.println("🎉 ВСЕ ТЕСТЫ ПРОЙДЕНЫ!");
    }
}

