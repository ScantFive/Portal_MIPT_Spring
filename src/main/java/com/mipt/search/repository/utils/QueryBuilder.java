package com.mipt.search.repository.utils;

import com.mipt.search.model.SearchCategory;
import com.mipt.search.model.SearchFilter;
import com.mipt.search.model.SearchQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Утилитный класс для построения SQL-запросов на основе SearchQuery. */
public final class QueryBuilder {

  private QueryBuilder() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Добавляет условия поиска по тексту в WHERE-клаузу. Поддерживает: - минус-слова (например:
   * -игровой) - слова, которые должны отсутствовать - слова в кавычках (например: "игровой
   * ноутбук") - точные фразы, которые должны присутствовать - обычные слова - слова, которые должны
   * присутствовать
   *
   * @param search поисковый запрос
   * @param params список параметров запроса
   * @param whereConditions список условий WHERE
   * @param tablePrefix префикс таблицы (может быть пустой строкой или "a.")
   */
  public static void addTextSearchCondition(
      SearchQuery search, List<Object> params, List<String> whereConditions, String tablePrefix) {
    Optional.ofNullable(search.getSearchText())
        .filter(text -> !text.trim().isEmpty())
        .ifPresent(
            text -> {
              SearchTextParser parser = new SearchTextParser(text);

              if (!parser.hasAnyConditions()) {
                return;
              }

              String prefix = tablePrefix.isEmpty() ? "" : tablePrefix;
              List<String> textConditions = new ArrayList<>();

              // Обязательные фразы в кавычках (должны присутствовать точно)
              if (parser.hasRequiredPhrases()) {
                for (String phrase : parser.getRequiredPhrases()) {
                  String phrasePattern = "%" + phrase + "%";
                  textConditions.add(
                      String.format(
                          "(%sname ILIKE ? OR %sdescription ILIKE ? OR %scategory ILIKE ?)",
                          prefix, prefix, prefix));
                  params.add(phrasePattern);
                  params.add(phrasePattern);
                  params.add(phrasePattern);
                }
              }

              // Обычные слова (хотя бы одно должно присутствовать)
              if (parser.hasNormalWords()) {
                List<String> normalWordConditions = new ArrayList<>();
                for (String word : parser.getNormalWords()) {
                  String wordPattern = "%" + word + "%";
                  normalWordConditions.add(
                      String.format(
                          "(%sname ILIKE ? OR %sdescription ILIKE ? OR %scategory ILIKE ?)",
                          prefix, prefix, prefix));
                  params.add(wordPattern);
                  params.add(wordPattern);
                  params.add(wordPattern);
                }
                // Объединяем обычные слова через OR (хотя бы одно должно совпасть)
                if (!normalWordConditions.isEmpty()) {
                  textConditions.add("(" + String.join(" OR ", normalWordConditions) + ")");
                }
              }

              // Исключаемые слова (минус-слова, не должны присутствовать)
              if (parser.hasExcludedWords()) {
                for (String excludedWord : parser.getExcludedWords()) {
                  String excludePattern = "%" + excludedWord + "%";
                  textConditions.add(
                      String.format(
                          "NOT (%sname ILIKE ? OR %sdescription ILIKE ? OR %scategory ILIKE ?)",
                          prefix, prefix, prefix));
                  params.add(excludePattern);
                  params.add(excludePattern);
                  params.add(excludePattern);
                }
              }

              // Объединяем все условия через AND
              if (!textConditions.isEmpty()) {
                whereConditions.add("(" + String.join(" AND ", textConditions) + ")");
              }
            });
  }

  /**
   * Добавляет условия поиска по типу в WHERE-клаузу.
   *
   * @param search поисковый запрос
   * @param params список параметров запроса
   * @param whereConditions список условий WHERE
   * @param tablePrefix префикс таблицы (может быть пустой строкой или "a.")
   */
  public static void addTypeCondition(
      SearchQuery search, List<Object> params, List<String> whereConditions, String tablePrefix) {
    Optional.ofNullable(search.getType())
        .ifPresent(
            type -> {
              String prefix = tablePrefix.isEmpty() ? "" : tablePrefix;
              whereConditions.add(prefix + "type = ?");
              params.add(type.toString());
            });
  }

  /**
   * Добавляет условия поиска по категориям в WHERE-клаузу.
   *
   * @param search поисковый запрос
   * @param params список параметров запроса
   * @param whereConditions список условий WHERE
   * @param tablePrefix префикс таблицы (может быть пустой строкой или "a.")
   */
  public static void addCategoryCondition(
      SearchQuery search, List<Object> params, List<String> whereConditions, String tablePrefix) {
    Optional.ofNullable(search.getCategory())
        .filter(categories -> !categories.isEmpty())
        .ifPresent(
            categories -> {
              List<String> categoryConditions = new ArrayList<>();
              String prefix = tablePrefix.isEmpty() ? "" : tablePrefix;

              categories.stream()
                  .filter(SearchCategory::isActive)
                  .forEach(
                      category ->
                          Optional.ofNullable(category.getCategoryTitle())
                              .ifPresent(
                                  title -> {
                                    categoryConditions.add(prefix + "category ILIKE ?");
                                    params.add("%" + title + "%");
                                  }));

              if (!categoryConditions.isEmpty()) {
                whereConditions.add("(" + String.join(" OR ", categoryConditions) + ")");
              }
            });
  }

  /**
   * Добавляет условия фильтрации по параметрам в WHERE-клаузу.
   *
   * @param search поисковый запрос
   * @param params список параметров запроса
   * @param whereConditions список условий WHERE
   * @param tablePrefix префикс таблицы (может быть пустой строкой или "a.")
   */
  public static void addFilterConditions(
      SearchQuery search, List<Object> params, List<String> whereConditions, String tablePrefix) {
    Optional.ofNullable(search.getFilters())
        .filter(filters -> !filters.isEmpty())
        .ifPresent(
            filters -> {
              String prefix = tablePrefix.isEmpty() ? "" : tablePrefix;

              filters.stream()
                  .filter(SearchFilter::isActive)
                  .forEach(
                      filter ->
                          Optional.ofNullable(filter.getFilterTitle())
                              .ifPresent(
                                  title -> {
                                    Optional<BigDecimal> fromValue =
                                        Optional.ofNullable(filter.getFromValue());
                                    Optional<BigDecimal> toValue =
                                        Optional.ofNullable(filter.getToValue());

                                    if (fromValue.isPresent() && toValue.isPresent()) {
                                      whereConditions.add(prefix + title + " BETWEEN ? AND ?");
                                      params.add(fromValue.get());
                                      params.add(toValue.get());
                                    } else if (fromValue.isPresent()) {
                                      whereConditions.add(prefix + title + " >= ?");
                                      params.add(fromValue.get());
                                    } else if (toValue.isPresent()) {
                                      whereConditions.add(prefix + title + " <= ?");
                                      params.add(toValue.get());
                                    }
                                  }));
            });
  }

  /**
   * Добавляет ORDER BY клаузу в SQL-запрос.
   *
   * @param search поисковый запрос
   * @param sqlQuery строитель SQL-запроса
   * @param defaultOrderBy строка сортировки по умолчанию (например, "created_at DESC")
   * @param tablePrefix префикс таблицы для полей (может быть пустой строкой или "a.")
   */
  public static void addOrderByClause(
      SearchQuery search, StringBuilder sqlQuery, String defaultOrderBy, String tablePrefix) {
    Optional.ofNullable(search.getSortOrder())
        .ifPresentOrElse(
            sortOrder -> {
              String prefix = tablePrefix.isEmpty() ? "" : tablePrefix;
              switch (sortOrder) {
                case NEWEST:
                  sqlQuery.append(" ORDER BY ").append(prefix).append("created_at DESC");
                  break;
                case OLDEST:
                  sqlQuery.append(" ORDER BY ").append(prefix).append("created_at ASC");
                  break;
                case CHEAPEST:
                  sqlQuery.append(" ORDER BY ").append(prefix).append("price ASC");
                  break;
                case EXPENSIVE:
                  sqlQuery.append(" ORDER BY ").append(prefix).append("price DESC");
                  break;
              }
            },
            () -> sqlQuery.append(" ORDER BY ").append(defaultOrderBy));
  }
}
