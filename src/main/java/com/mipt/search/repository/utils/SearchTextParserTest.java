package com.mipt.search.repository.utils;

/** Тестовый класс для проверки работы SearchTextParser. */
public class SearchTextParserTest {

  public static void main(String[] args) {
    testParser();
  }

  private static void testParser() {
    System.out.println("=== Тестирование SearchTextParser ===\n");

    // Тест 1: Простой поиск
    testCase("ноутбук", "Простой поиск");

    // Тест 2: Поиск с минус-словом в кавычках
    testCase("ноутбук \"-игровой\"", "Поиск с минус-словом в кавычках");

    // Тест 3: Поиск с фразой в кавычках
    testCase("\"игровой ноутбук\"", "Поиск с фразой в кавычках");

    // Тест 4: Комбинированный поиск
    testCase("ноутбук \"высокая производительность\" \"-бу\" \"-б/у\"", "Комбинированный поиск");

    // Тест 5: Множественные кавычки и минус-слова
    testCase(
        "\"новый ноутбук\" \"16 гб\" процессор \"-intel\" \"-бу\" \"-дефект\"",
        "Сложный комбинированный поиск");

    // Тест 6: Пустая строка
    testCase("", "Пустая строка");

    // Тест 7: Только минус-слова
    testCase("\"-игровой\" \"-дешевый\"", "Только минус-слова");

    // Тест 8: Только кавычки
    testCase("\"точная фраза\"", "Только кавычки");

    // Тест 9: Минус без кавычек (должен игнорироваться как обычное слово)
    testCase("ноутбук -игровой", "Минус без кавычек (обычные слова)");

    // Тест 10: Смешанный - обычные слова и операторы
    testCase("смартфон \"большой экран\" камера \"-китайский\" \"-дешевый\"", "Смешанный поиск");
  }

  private static void testCase(String input, String description) {
    System.out.println("Тест: " + description);
    System.out.println("Вход: \"" + input + "\"");

    SearchTextParser parser = new SearchTextParser(input);

    System.out.println("Обязательные фразы: " + parser.getRequiredPhrases());
    System.out.println("Обычные слова: " + parser.getNormalWords());
    System.out.println("Исключаемые слова: " + parser.getExcludedWords());
    System.out.println("Есть условия: " + parser.hasAnyConditions());
    System.out.println();
  }
}
