package com.mipt.search.repository;

import com.mipt.search.model.SearchCategory;
import com.mipt.search.model.SearchHistory;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.model.SearchSortOrder;
import com.mipt.search.model.SearchType;
import com.mipt.util.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Репозиторий для работы с историей поисковых запросов. */
public class SearchHistoryRepository {

  /** Сохраняет поисковый запрос в историю */
  public static void saveSearchHistory(UUID userId, SearchQuery query, int resultsCount) {
    if (userId == null) {
      return; // Не сохраняем историю для неавторизованных пользователей
    }

    String sql =
        "INSERT INTO search_history (user_id, search_text, search_type, categories, sort_order, results_count) "
            + "VALUES (?, ?, ?::VARCHAR, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setString(2, query.getSearchText());
      pstmt.setString(3, query.getType() != null ? query.getType().name() : null);

      // Преобразуем список категорий в массив SQL
      if (query.getCategory() != null && !query.getCategory().isEmpty()) {
        String[] categories =
            query.getCategory().stream()
                .map(SearchCategory::getCategoryTitle)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        Array sqlArray = conn.createArrayOf("VARCHAR", categories);
        pstmt.setArray(4, sqlArray);
      } else {
        pstmt.setArray(4, null);
      }

      pstmt.setString(5, query.getSortOrder() != null ? query.getSortOrder().name() : null);
      pstmt.setInt(6, resultsCount);

      pstmt.executeUpdate();
    } catch (SQLException e) {
      // Логируем ошибку, но не прерываем выполнение основного запроса
      System.err.println("Ошибка сохранения истории поиска: " + e.getMessage());
    }
  }

  /** Получает историю поисковых запросов пользователя */
  public static List<SearchHistory> getUserSearchHistory(UUID userId, int limit) {
    List<SearchHistory> history = new ArrayList<>();

    String sql =
        "SELECT id, user_id, search_text, search_type, categories, sort_order, results_count, created_at "
            + "FROM search_history "
            + "WHERE user_id = ? "
            + "ORDER BY created_at DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setInt(2, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          SearchHistory item = mapResultSetToSearchHistory(rs);
          history.add(item);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения истории поиска: " + e.getMessage(), e);
    }

    return history;
  }

  /** Получает последние уникальные поисковые запросы пользователя (по тексту) */
  public static List<String> getRecentSearchTexts(UUID userId, int limit) {
    List<String> searchTexts = new ArrayList<>();

    String sql =
        "SELECT DISTINCT ON (search_text) search_text "
            + "FROM search_history "
            + "WHERE user_id = ? AND search_text IS NOT NULL AND search_text != '' "
            + "ORDER BY search_text, created_at DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setInt(2, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          searchTexts.add(rs.getString("search_text"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения текстов поиска: " + e.getMessage(), e);
    }

    return searchTexts;
  }

  /** Получает популярные поисковые запросы пользователя */
  public static List<SearchHistory> getPopularSearches(UUID userId, int limit) {
    List<SearchHistory> history = new ArrayList<>();

    String sql =
        "SELECT search_text, search_type, categories, sort_order, "
            + "COUNT(*) as search_count, "
            + "MAX(created_at) as last_search, "
            + "AVG(results_count) as avg_results "
            + "FROM search_history "
            + "WHERE user_id = ? "
            + "GROUP BY search_text, search_type, categories, sort_order "
            + "ORDER BY search_count DESC, last_search DESC "
            + "LIMIT ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setInt(2, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          SearchHistory item = new SearchHistory();
          item.setUserId(userId);
          item.setSearchText(rs.getString("search_text"));

          String typeStr = rs.getString("search_type");
          if (typeStr != null) {
            item.setSearchType(SearchType.valueOf(typeStr));
          }

          Array categoriesArray = rs.getArray("categories");
          if (categoriesArray != null) {
            String[] categories = (String[]) categoriesArray.getArray();
            item.setCategories(List.of(categories));
          }

          String sortOrderStr = rs.getString("sort_order");
          if (sortOrderStr != null) {
            item.setSortOrder(SearchSortOrder.valueOf(sortOrderStr));
          }

          item.setResultsCount((int) rs.getDouble("avg_results"));
          item.setCreatedAt(rs.getTimestamp("last_search").toLocalDateTime());

          history.add(item);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения популярных запросов: " + e.getMessage(), e);
    }

    return history;
  }

  /** Удаляет историю поиска пользователя */
  public static void clearUserSearchHistory(UUID userId) {
    String sql = "DELETE FROM search_history WHERE user_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления истории поиска: " + e.getMessage(), e);
    }
  }

  /** Удаляет конкретную запись из истории */
  public static void deleteSearchHistoryEntry(UUID historyId, UUID userId) {
    String sql = "DELETE FROM search_history WHERE id = ? AND user_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, historyId);
      pstmt.setObject(2, userId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления записи истории: " + e.getMessage(), e);
    }
  }

  /** Маппинг ResultSet в объект SearchHistory */
  private static SearchHistory mapResultSetToSearchHistory(ResultSet rs) throws SQLException {
    SearchHistory item = new SearchHistory();
    item.setId((UUID) rs.getObject("id"));
    item.setUserId((UUID) rs.getObject("user_id"));
    item.setSearchText(rs.getString("search_text"));

    String typeStr = rs.getString("search_type");
    if (typeStr != null) {
      item.setSearchType(SearchType.valueOf(typeStr));
    }

    Array categoriesArray = rs.getArray("categories");
    if (categoriesArray != null) {
      String[] categories = (String[]) categoriesArray.getArray();
      item.setCategories(List.of(categories));
    }

    String sortOrderStr = rs.getString("sort_order");
    if (sortOrderStr != null) {
      item.setSortOrder(SearchSortOrder.valueOf(sortOrderStr));
    }

    item.setResultsCount(rs.getInt("results_count"));

    Timestamp timestamp = rs.getTimestamp("created_at");
    if (timestamp != null) {
      item.setCreatedAt(timestamp.toLocalDateTime());
    }

    return item;
  }
}
