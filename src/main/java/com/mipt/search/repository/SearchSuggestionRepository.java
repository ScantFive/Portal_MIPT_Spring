package com.mipt.search.repository;

import com.mipt.search.model.SearchSuggestion;
import com.mipt.search.model.SuggestionType;
import com.mipt.util.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Репозиторий для работы с контекстными подсказками поиска. */
public class SearchSuggestionRepository {

  /**
   * Получает подсказки из истории поиска пользователя
   *
   * @param userId ID пользователя
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit Максимальное количество подсказок
   * @return Список подсказок из истории
   */
  public static List<SearchSuggestion> getHistorySuggestions(
      UUID userId, String prefix, int limit) {
    List<SearchSuggestion> suggestions = new ArrayList<>();

    String sql =
        "SELECT DISTINCT ON (search_text) search_text, "
            + "COUNT(*) OVER (PARTITION BY search_text) as usage_count, "
            + "MAX(created_at) OVER (PARTITION BY search_text) as last_used "
            + "FROM search_history "
            + "WHERE user_id = ? "
            + "AND search_text IS NOT NULL "
            + "AND search_text != '' "
            + (prefix != null && !prefix.isEmpty() ? "AND search_text ILIKE ? " : "")
            + "ORDER BY search_text, last_used DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      int paramIndex = 1;
      pstmt.setObject(paramIndex++, userId);
      if (prefix != null && !prefix.isEmpty()) {
        pstmt.setString(paramIndex++, prefix + "%");
      }
      pstmt.setInt(paramIndex, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String text = rs.getString("search_text");
          int usageCount = rs.getInt("usage_count");
          double relevance = usageCount; // Релевантность = количество использований

          SearchSuggestion suggestion =
              new SearchSuggestion(text, SuggestionType.HISTORY, relevance);
          suggestions.add(suggestion);
        }
      }
    } catch (SQLException e) {
      System.err.println("Ошибка получения подсказок из истории: " + e.getMessage());
    }

    return suggestions;
  }

  /**
   * Получает популярные подсказки на основе частоты использования
   *
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit Максимальное количество подсказок
   * @return Список популярных подсказок
   */
  public static List<SearchSuggestion> getPopularSuggestions(String prefix, int limit) {
    List<SearchSuggestion> suggestions = new ArrayList<>();

    String sql =
        "SELECT search_text, "
            + "COUNT(*) as search_count, "
            + "AVG(results_count) as avg_results "
            + "FROM search_history "
            + "WHERE search_text IS NOT NULL "
            + "AND search_text != '' "
            + (prefix != null && !prefix.isEmpty() ? "AND search_text ILIKE ? " : "")
            + "GROUP BY search_text "
            + "HAVING COUNT(*) > 1 "
            + "ORDER BY search_count DESC, avg_results DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      int paramIndex = 1;
      if (prefix != null && !prefix.isEmpty()) {
        pstmt.setString(paramIndex++, prefix + "%");
      }
      pstmt.setInt(paramIndex, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String text = rs.getString("search_text");
          int searchCount = rs.getInt("search_count");
          double avgResults = rs.getDouble("avg_results");
          double relevance = searchCount * 10 + avgResults;

          SearchSuggestion suggestion =
              new SearchSuggestion(text, SuggestionType.POPULAR, relevance);
          suggestion.setResultsCount((int) avgResults);
          suggestions.add(suggestion);
        }
      }
    } catch (SQLException e) {
      System.err.println("Ошибка получения популярных подсказок: " + e.getMessage());
    }

    return suggestions;
  }

  /**
   * Получает подсказки автодополнения из названий объявлений
   *
   * @param prefix Префикс для поиска
   * @param limit Максимальное количество подсказок
   * @return Список подсказок автодополнения
   */
  public static List<SearchSuggestion> getAutocompleteSuggestions(String prefix, int limit) {
    List<SearchSuggestion> suggestions = new ArrayList<>();

    if (prefix == null || prefix.trim().isEmpty()) {
      return suggestions;
    }

    // Используем триграммный поиск и полнотекстовый поиск
    String sql =
        "SELECT name, "
            + "similarity(name, ?) as sim_score, "
            + "COUNT(*) as ads_count "
            + "FROM advertisements "
            + "WHERE status = 'ACTIVE' "
            + "AND (name ILIKE ? OR similarity(name, ?) > 0.1) "
            + "GROUP BY name "
            + "ORDER BY sim_score DESC, ads_count DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      String searchPattern = "%" + prefix + "%";
      pstmt.setString(1, prefix);
      pstmt.setString(2, searchPattern);
      pstmt.setString(3, prefix);
      pstmt.setInt(4, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String text = rs.getString("name");
          double simScore = rs.getDouble("sim_score");
          int adsCount = rs.getInt("ads_count");
          double relevance = simScore * 100 + adsCount;

          SearchSuggestion suggestion =
              new SearchSuggestion(text, SuggestionType.AUTOCOMPLETE, relevance);
          suggestion.setResultsCount(adsCount);
          suggestions.add(suggestion);
        }
      }
    } catch (SQLException e) {
      System.err.println("Ошибка получения подсказок автодополнения: " + e.getMessage());
    }

    return suggestions;
  }

  /**
   * Получает подсказки категорий
   *
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit Максимальное количество подсказок
   * @return Список подсказок категорий
   */
  public static List<SearchSuggestion> getCategorySuggestions(String prefix, int limit) {
    List<SearchSuggestion> suggestions = new ArrayList<>();

    String sql =
        "SELECT category, "
            + "COUNT(*) as ads_count "
            + "FROM advertisements "
            + "WHERE status = 'ACTIVE' "
            + "AND category IS NOT NULL "
            + (prefix != null && !prefix.isEmpty() ? "AND category ILIKE ? " : "")
            + "GROUP BY category "
            + "ORDER BY ads_count DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      int paramIndex = 1;
      if (prefix != null && !prefix.isEmpty()) {
        pstmt.setString(paramIndex++, "%" + prefix + "%");
      }
      pstmt.setInt(paramIndex, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String category = rs.getString("category");
          int adsCount = rs.getInt("ads_count");

          SearchSuggestion suggestion =
              new SearchSuggestion(category, SuggestionType.CATEGORY, (double) adsCount);
          suggestion.setResultsCount(adsCount);
          suggestion.setMetadata("category");
          suggestions.add(suggestion);
        }
      }
    } catch (SQLException e) {
      System.err.println("Ошибка получения подсказок категорий: " + e.getMessage());
    }

    return suggestions;
  }

  /**
   * Получает трендовые подсказки (популярные запросы за последнее время)
   *
   * @param prefix Префикс для фильтрации (может быть null)
   * @param daysBack Количество дней назад для анализа трендов
   * @param limit Максимальное количество подсказок
   * @return Список трендовых подсказок
   */
  public static List<SearchSuggestion> getTrendingSuggestions(
      String prefix, int daysBack, int limit) {
    List<SearchSuggestion> suggestions = new ArrayList<>();

    String sql =
        "SELECT search_text, "
            + "COUNT(*) as search_count, "
            + "COUNT(DISTINCT user_id) as unique_users, "
            + "AVG(results_count) as avg_results "
            + "FROM search_history "
            + "WHERE search_text IS NOT NULL "
            + "AND search_text != '' "
            + "AND created_at >= NOW() - INTERVAL '"
            + daysBack
            + " days' "
            + (prefix != null && !prefix.isEmpty() ? "AND search_text ILIKE ? " : "")
            + "GROUP BY search_text "
            + "HAVING COUNT(DISTINCT user_id) > 1 "
            + "ORDER BY search_count DESC, unique_users DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      int paramIndex = 1;
      if (prefix != null && !prefix.isEmpty()) {
        pstmt.setString(paramIndex++, prefix + "%");
      }
      pstmt.setInt(paramIndex, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String text = rs.getString("search_text");
          int searchCount = rs.getInt("search_count");
          int uniqueUsers = rs.getInt("unique_users");
          double avgResults = rs.getDouble("avg_results");
          // Релевантность основана на количестве поисков и уникальных пользователей
          double relevance = searchCount * 5 + uniqueUsers * 20 + avgResults;

          SearchSuggestion suggestion =
              new SearchSuggestion(text, SuggestionType.TRENDING, relevance);
          suggestion.setResultsCount((int) avgResults);
          suggestions.add(suggestion);
        }
      }
    } catch (SQLException e) {
      System.err.println("Ошибка получения трендовых подсказок: " + e.getMessage());
    }

    return suggestions;
  }

  /**
   * Получает персонализированные подсказки на основе предпочтений пользователя
   *
   * @param userId ID пользователя
   * @param prefix Префикс для фильтрации (может быть null)
   * @param limit Максимальное количество подсказок
   * @return Список персонализированных подсказок
   */
  public static List<SearchSuggestion> getPersonalizedSuggestions(
      UUID userId, String prefix, int limit) {
    List<SearchSuggestion> suggestions = new ArrayList<>();

    // Анализируем категории, которые пользователь чаще всего искал
    String sql =
        "WITH user_categories AS ( "
            + "  SELECT unnest(categories) as category, COUNT(*) as cat_count "
            + "  FROM search_history "
            + "  WHERE user_id = ? "
            + "  AND categories IS NOT NULL "
            + "  GROUP BY category "
            + ") "
            + "SELECT a.name, "
            + "  uc.cat_count, "
            + "  COUNT(*) as ads_count "
            + "FROM advertisements a "
            + "JOIN user_categories uc ON a.category ILIKE '%' || uc.category || '%' "
            + "WHERE a.status = 'ACTIVE' "
            + (prefix != null && !prefix.isEmpty() ? "AND a.name ILIKE ? " : "")
            + "GROUP BY a.name, uc.cat_count "
            + "ORDER BY uc.cat_count DESC, ads_count DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      int paramIndex = 1;
      pstmt.setObject(paramIndex++, userId);
      if (prefix != null && !prefix.isEmpty()) {
        pstmt.setString(paramIndex++, prefix + "%");
      }
      pstmt.setInt(paramIndex, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String text = rs.getString("name");
          int catCount = rs.getInt("cat_count");
          int adsCount = rs.getInt("ads_count");
          double relevance = catCount * 10 + adsCount;

          SearchSuggestion suggestion =
              new SearchSuggestion(text, SuggestionType.PERSONALIZED, relevance);
          suggestion.setResultsCount(adsCount);
          suggestions.add(suggestion);
        }
      }
    } catch (SQLException e) {
      System.err.println("Ошибка получения персонализированных подсказок: " + e.getMessage());
    }

    return suggestions;
  }
}
