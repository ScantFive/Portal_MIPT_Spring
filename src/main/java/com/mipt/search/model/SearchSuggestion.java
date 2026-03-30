package com.mipt.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Модель контекстной подсказки для поиска. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestion {
  /** Текст подсказки */
  private String text;

  /** Тип подсказки */
  private SuggestionType type;

  /** Количество результатов (если известно) */
  private Integer resultsCount;

  /** Релевантность/популярность подсказки (для сортировки) */
  private Double relevance;

  /** Дополнительные метаданные (категория, тип объявления и т.д.) */
  private String metadata;

  public SearchSuggestion(String text, SuggestionType type) {
    this.text = text;
    this.type = type;
  }

  public SearchSuggestion(String text, SuggestionType type, Double relevance) {
    this.text = text;
    this.type = type;
    this.relevance = relevance;
  }
}
