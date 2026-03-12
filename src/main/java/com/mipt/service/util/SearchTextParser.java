package com.mipt.service.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер поисковой строки с поддержкой: - минус-слов (слова в кавычках с минусом, которые должны
 * быть исключены, например: "-игровой") - слов в кавычках (точные фразы, например: "игровой
 * ноутбук") - обычных слов
 */
@Getter
public class SearchTextParser {

  // Паттерн для контента в кавычках (может быть обычная фраза или минус-слово)
  private static final Pattern QUOTED_PATTERN = Pattern.compile("\"([^\"]+)\"");
  private final List<String> requiredPhrases; // Слова/фразы в кавычках (обязательные)
  private final List<String> excludedWords; // Минус-слова (исключаемые)
  private final List<String> normalWords; // Обычные слова

  public SearchTextParser(String searchText) {
    this.requiredPhrases = new ArrayList<>();
    this.excludedWords = new ArrayList<>();
    this.normalWords = new ArrayList<>();

    if (searchText == null || searchText.trim().isEmpty()) {
      return;
    }

    parseSearchText(searchText);
  }

  /** Парсит поисковую строку, извлекая разные типы слов. */
  private void parseSearchText(String searchText) {
    String workingText = searchText;

    // Извлекаем содержимое в кавычках
    Matcher quotedMatcher = QUOTED_PATTERN.matcher(workingText);
    while (quotedMatcher.find()) {
      String content = quotedMatcher.group(1).trim();
      if (!content.isEmpty()) {
        // Проверяем, начинается ли содержимое с минуса
        if (content.startsWith("-")) {
          // Это минус-слово: "-слово"
          String word = content.substring(1).trim();
          if (!word.isEmpty()) {
            excludedWords.add(word);
          }
        } else {
          // Это обычная фраза в кавычках
          requiredPhrases.add(content);
        }
      }
    }
    // Удаляем найденные кавычки из текста
    workingText = quotedMatcher.replaceAll(" ");

    // Оставшиеся слова - обычные
    String[] words = workingText.trim().split("\\s+");
    for (String word : words) {
      String trimmedWord = word.trim();
      if (!trimmedWord.isEmpty()) {
        normalWords.add(trimmedWord);
      }
    }
  }

  /** Проверяет, есть ли какие-либо условия поиска. */
  public boolean hasAnyConditions() {
    return !requiredPhrases.isEmpty() || !excludedWords.isEmpty() || !normalWords.isEmpty();
  }

  /** Проверяет, есть ли обязательные фразы. */
  public boolean hasRequiredPhrases() {
    return !requiredPhrases.isEmpty();
  }

  /** Проверяет, есть ли исключаемые слова. */
  public boolean hasExcludedWords() {
    return !excludedWords.isEmpty();
  }

  /** Проверяет, есть ли обычные слова. */
  public boolean hasNormalWords() {
    return !normalWords.isEmpty();
  }
}
