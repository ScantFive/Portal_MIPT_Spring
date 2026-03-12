package com.mipt.repository.mainpage;

import com.mipt.config.DatabaseConfig;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Репозиторий для управления избранными объявлениями. Использует таблицу favorites для хранения
 * связей пользователь-объявление. Флаг is_favorite в advertisements обновляется автоматически через
 * триггеры. Для поиска по избранным используйте SearchRepository.getFavoriteAdverts()
 */

@Repository
@Transactional
public class FavoritesRepository {

  /** Проверить, является ли объявление избранным для конкретного пользователя */
  public static boolean isFavorite(UUID userId, UUID advertisementId) {
    String sql =
        "SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = ? AND advertisement_id = ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setObject(2, advertisementId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getBoolean(1);
        }
        return false;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка проверки избранного: " + e.getMessage(), e);
    }
  }

  /**
   * Добавить объявление в избранное для пользователя Флаг is_favorite обновится автоматически через
   * триггер
   */
  public static void addToFavorites(UUID userId, UUID advertisementId) {
    String sql =
        "INSERT INTO favorites (user_id, advertisement_id) VALUES (?, ?) "
            + "ON CONFLICT (user_id, advertisement_id) DO NOTHING";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setObject(2, advertisementId);
      pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Ошибка добавления в избранное: " + e.getMessage(), e);
    }
  }

  /**
   * Удалить объявление из избранного для пользователя Флаг is_favorite обновится автоматически
   * через триггер
   */
  public static void removeFromFavorites(UUID userId, UUID advertisementId) {
    String sql = "DELETE FROM favorites WHERE user_id = ? AND advertisement_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);
      pstmt.setObject(2, advertisementId);
      pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления из избранного: " + e.getMessage(), e);
    }
  }

  /** Получить количество избранных объявлений пользователя */
  public static long getFavoritesCount(UUID userId) {
    String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, userId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
        return 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения количества избранных: " + e.getMessage(), e);
    }
  }

  /** Получить количество пользователей, добавивших объявление в избранное */
  public static long getFavoriteUsersCount(UUID advertisementId) {
    String sql = "SELECT COUNT(*) FROM favorites WHERE advertisement_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, advertisementId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
        return 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения количества пользователей: " + e.getMessage(), e);
    }
  }
}
