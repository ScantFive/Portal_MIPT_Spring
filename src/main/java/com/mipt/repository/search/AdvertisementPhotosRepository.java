package com.mipt.repository.search;

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

//Репозиторий для работы с фотографиями объявлений
public class AdvertisementPhotosRepository {

  /**
   * Добавить фотографию к объявлению
   *
   * @param advertisementId ID объявления
   * @param photoUrl URL фотографии
   * @param displayOrder порядок отображения (необязательно)
   * @return ID добавленной фотографии
   */
  public static long addPhoto(UUID advertisementId, String photoUrl, Integer displayOrder) {
    String sql =
        "INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) "
            + "VALUES (?, ?, ?) RETURNING id";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, advertisementId);
      pstmt.setString(2, photoUrl);
      pstmt.setInt(3, displayOrder != null ? displayOrder : 0);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("id");
        }
        throw new RuntimeException("Не удалось добавить фотографию");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка добавления фотографии: " + e.getMessage(), e);
    }
  }

  /**
   * Добавить несколько фотографий к объявлению
   *
   * @param advertisementId ID объявления
   * @param photoUrls список URL фотографий
   */
  public static void addPhotos(UUID advertisementId, List<String> photoUrls) {
    if (photoUrls == null || photoUrls.isEmpty()) {
      return;
    }

    String sql =
        "INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) "
            + "VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      int order = 0;
      for (String photoUrl : photoUrls) {
        if (photoUrl != null && !photoUrl.isBlank()) {
          pstmt.setObject(1, advertisementId);
          pstmt.setString(2, photoUrl);
          pstmt.setInt(3, order++);
          pstmt.addBatch();
        }
      }

      pstmt.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка добавления фотографий: " + e.getMessage(), e);
    }
  }

  /**
   * Получить все фотографии объявления
   *
   * @param advertisementId ID объявления
   * @return список URL фотографий в порядке отображения
   */
  public static List<String> getPhotos(UUID advertisementId) {
    String sql =
        "SELECT photo_url FROM advertisement_photos "
            + "WHERE advertisement_id = ? ORDER BY display_order";

    List<String> photos = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, advertisementId);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          photos.add(rs.getString("photo_url"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения фотографий: " + e.getMessage(), e);
    }

    return photos;
  }

  /**
   * Удалить фотографию по ID
   *
   * @param photoId ID фотографии
   * @return true, если фотография была удалена
   */
  public static boolean deletePhoto(long photoId) {
    String sql = "DELETE FROM advertisement_photos WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, photoId);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления фотографии: " + e.getMessage(), e);
    }
  }

  /**
   * Удалить фотографию по URL
   *
   * @param advertisementId ID объявления
   * @param photoUrl URL фотографии
   * @return true, если фотография была удалена
   */
  public static boolean deletePhotoByUrl(UUID advertisementId, String photoUrl) {
    String sql = "DELETE FROM advertisement_photos WHERE advertisement_id = ? AND photo_url = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, advertisementId);
      pstmt.setString(2, photoUrl);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления фотографии: " + e.getMessage(), e);
    }
  }

  /**
   * Удалить все фотографии объявления
   *
   * @param advertisementId ID объявления
   * @return количество удаленных фотографий
   */
  public static int deleteAllPhotos(UUID advertisementId) {
    String sql = "DELETE FROM advertisement_photos WHERE advertisement_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, advertisementId);
      return pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления фотографий: " + e.getMessage(), e);
    }
  }

  /**
   * Обновить порядок отображения фотографии
   *
   * @param photoId ID фотографии
   * @param newOrder новый порядок отображения
   * @return true, если порядок был обновлен
   */
  public static boolean updatePhotoOrder(long photoId, int newOrder) {
    String sql = "UPDATE advertisement_photos SET display_order = ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, newOrder);
      pstmt.setLong(2, photoId);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка обновления порядка фотографии: " + e.getMessage(), e);
    }
  }

  /**
   * Получить количество фотографий у объявления
   *
   * @param advertisementId ID объявления
   * @return количество фотографий
   */
  public static int getPhotosCount(UUID advertisementId) {
    String sql = "SELECT COUNT(*) FROM advertisement_photos WHERE advertisement_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setObject(1, advertisementId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка подсчета фотографий: " + e.getMessage(), e);
    }

    return 0;
  }
}
