package com.mipt.repository.search;

import com.mipt.model.mainpage.ShortAdvert;
import com.mipt.repository.mainpage.FavoritesRepository;
import com.mipt.model.search.SearchQuery;
import com.mipt.service.util.AdvertMapper;
import com.mipt.service.util.QueryBuilder;
import com.mipt.config.DatabaseConfig;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional

//Репозиторий для поиска объявлений
public class SearchRepository {

  private static final int DESCRIPTION_PREVIEW_LENGTH = 100;
  private static final String DEFAULT_ORDER_BY = "created_at DESC";

  public static List<ShortAdvert> getAdverts(long limit, long offset, SearchQuery search) {
    return getAdverts(limit, offset, search, null);
  }

  public static List<ShortAdvert> getAdverts(
      long limit, long offset, SearchQuery search, UUID userId) {
    StringBuilder sqlQuery =
        new StringBuilder(
            "SELECT a.*, "
                + "(SELECT ARRAY_AGG(photo_url ORDER BY display_order) "
                + " FROM (SELECT photo_url, display_order FROM advertisement_photos "
                + "       WHERE advertisement_id = a.id ORDER BY display_order LIMIT 5) sub) AS photos "
                + "FROM advertisements a");
    List<Object> params = new ArrayList<>();
    List<String> whereConditions = new ArrayList<>();

    // Используем QueryBuilder для построения условий WHERE с префиксом "a."
    QueryBuilder.addTextSearchCondition(search, params, whereConditions, "a.");
    QueryBuilder.addTypeCondition(search, params, whereConditions, "a.");
    QueryBuilder.addCategoryCondition(search, params, whereConditions, "a.");
    QueryBuilder.addFilterConditions(search, params, whereConditions, "a.");

    if (!whereConditions.isEmpty()) {
      sqlQuery.append(" WHERE ").append(String.join(" AND ", whereConditions));
    }

    // Добавляем сортировку
    QueryBuilder.addOrderByClause(search, sqlQuery, DEFAULT_ORDER_BY, "a.");

    sqlQuery.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString())) {

      for (int i = 0; i < params.size(); i++) {
        pstmt.setObject(i + 1, params.get(i));
      }

      try (ResultSet rowAdvertData = pstmt.executeQuery()) {
        List<ShortAdvert> adverts = new ArrayList<>();
        while (rowAdvertData.next()) {
          ShortAdvert advert =
              AdvertMapper.rowAdverToShortAdvert(rowAdvertData, DESCRIPTION_PREVIEW_LENGTH);

          // Если передан userId, проверяем избранное для конкретного пользователя
          if (userId != null) {
            boolean isFavorite = FavoritesRepository.isFavorite(userId, advert.getAdvertId());
            advert.setFavorite(isFavorite);
          }

          adverts.add(advert);
        }
        return adverts;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения объявлений: " + e.getMessage(), e);
    }
  }

  /** Получить общее количество объявлений, соответствующих поисковому запросу */
  public static long getAdvertsCount(SearchQuery search) {
    StringBuilder sqlQuery = new StringBuilder("SELECT COUNT(*) FROM advertisements");
    List<Object> params = new ArrayList<>();
    List<String> whereConditions = new ArrayList<>();

    // Используем QueryBuilder для построения условий WHERE
    QueryBuilder.addTextSearchCondition(search, params, whereConditions, "");
    QueryBuilder.addTypeCondition(search, params, whereConditions, "");
    QueryBuilder.addCategoryCondition(search, params, whereConditions, "");
    QueryBuilder.addFilterConditions(search, params, whereConditions, "");

    if (!whereConditions.isEmpty()) {
      sqlQuery.append(" WHERE ").append(String.join(" AND ", whereConditions));
    }

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString())) {

      for (int i = 0; i < params.size(); i++) {
        pstmt.setObject(i + 1, params.get(i));
      }

      try (ResultSet resultSet = pstmt.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getLong(1);
        }
        return 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка подсчета объявлений: " + e.getMessage(), e);
    }
  }

  /** Получить избранные объявления пользователя с возможностью фильтрации */
  public static List<ShortAdvert> getFavoriteAdverts(
      UUID userId, long limit, long offset, SearchQuery search) {
    StringBuilder sqlQuery =
        new StringBuilder(
            "SELECT a.*, "
                + "(SELECT ARRAY_AGG(photo_url ORDER BY display_order) "
                + " FROM (SELECT photo_url, display_order FROM advertisement_photos "
                + "       WHERE advertisement_id = a.id ORDER BY display_order LIMIT 5) sub) AS photos, "
                + "f.created_at AS favorite_created_at "
                + "FROM advertisements a "
                + "INNER JOIN favorites f ON a.id = f.advertisement_id "
                + "WHERE f.user_id = ?");

    List<Object> params = new ArrayList<>();
    params.add(userId);
    List<String> whereConditions = new ArrayList<>();

    // Используем QueryBuilder для построения условий WHERE с префиксом "a."
    QueryBuilder.addTextSearchCondition(search, params, whereConditions, "a.");
    QueryBuilder.addTypeCondition(search, params, whereConditions, "a.");
    QueryBuilder.addCategoryCondition(search, params, whereConditions, "a.");
    QueryBuilder.addFilterConditions(search, params, whereConditions, "a.");

    if (!whereConditions.isEmpty()) {
      sqlQuery.append(" AND ").append(String.join(" AND ", whereConditions));
    }

    // Добавляем сортировку (для избранных сортируем по дате добавления в избранное)
    QueryBuilder.addOrderByClause(search, sqlQuery, "f.created_at DESC", "a.");

    sqlQuery.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString())) {

      for (int i = 0; i < params.size(); i++) {
        pstmt.setObject(i + 1, params.get(i));
      }

      try (ResultSet rowAdvertData = pstmt.executeQuery()) {
        List<ShortAdvert> adverts = new ArrayList<>();
        while (rowAdvertData.next()) {
          ShortAdvert advert = AdvertMapper.rowAdverToShortAdvert(rowAdvertData, 100);
          advert.setFavorite(true); // Все объявления в результате - избранные
          adverts.add(advert);
        }
        return adverts;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения избранных объявлений: " + e.getMessage(), e);
    }
  }
}
