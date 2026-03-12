package com.mipt.repository.advertisement;

import com.mipt.config.DatabaseConfig;
import com.mipt.model.advertisement.Advertisement;
import com.mipt.model.advertisement.AdvertisementStatus;
import com.mipt.model.advertisement.Category;
import com.mipt.model.advertisement.Type;
import lombok.Data;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.Instant;
import java.util.*;
@Repository
@Transactional
@Data
public class AdvertisementRepository implements AdvertisementRep {

  @Override
  public Advertisement create(Advertisement advertisement) {
    System.out.println("🔍 DEBUG: Starting to create advertisement...");
    advertisement.validateToCreate();
    System.out.println("🔍 DEBUG: Validation passed");

    try (Connection connection = DatabaseConfig.getConnection()) {
      System.out.println("🔍 DEBUG: Got database connection");
      connection.setAutoCommit(false);

      try {
        String sql =
            "INSERT INTO advertisements (id, status, author, type, category, name, price, description, is_favorite, created_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
          statement.setObject(1, advertisement.getId());
          statement.setString(2, advertisement.getStatus().name());
          statement.setObject(3, advertisement.getAuthorId());
          statement.setString(4, advertisement.getType().name());

          // Теперь категория - одно значение, а не список
          statement.setString(5, advertisement.getCategory().name()); // Используем enum name

          statement.setString(6, advertisement.getName());

          if (advertisement.getPrice() != null) {
            statement.setLong(7, advertisement.getPrice());
          } else {
            statement.setNull(7, Types.BIGINT);
          }

          statement.setString(8, advertisement.getDescription());
          statement.setBoolean(9, advertisement.isFavorite());
          statement.setTimestamp(10, Timestamp.from(advertisement.getCreatedAt()));

          System.out.println("🔍 DEBUG: Executing insert...");
          int rows = statement.executeUpdate();
          System.out.println("🔍 DEBUG: Inserted " + rows + " rows");
        }

        System.out.println("🔍 DEBUG: Saving photos...");
        savePhotoUrls(advertisement, connection);

        connection.commit();
        System.out.println("🔍 DEBUG: Transaction committed");

        return findById(advertisement.getId()).orElseThrow(() ->
            new RuntimeException(
                "Failed to retrieve created advertisement: " + advertisement.getId())
        );

      } catch (SQLException e) {
        System.err.println("❌ ERROR in transaction: " + e.getMessage());
        connection.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create advertisement", e);
    }
  }

  @Override
  public Advertisement publish(Advertisement advertisement) {
    advertisement.validateToPublish();
    advertisement.setStatus(AdvertisementStatus.ACTIVE);
    return update(advertisement);
  }

  @Override
  public Advertisement pause(Advertisement advertisement) {
    if (advertisement.getStatus() != AdvertisementStatus.ACTIVE) {
      throw new IllegalArgumentException(
          "Can only pause active advertisements. Current status: " + advertisement.getStatus());
    }
    advertisement.setStatus(AdvertisementStatus.PAUSED);
    return update(advertisement);
  }

  @Override
  public Optional<Advertisement> findById(UUID id) {
    String sql = "SELECT * FROM advertisements WHERE id = ?";

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setObject(1, id);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        Advertisement advertisement = mapResultSetToAdvertisement(resultSet);
        loadPhotoUrls(advertisement, connection);
        return Optional.of(advertisement);
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find advertisement by id: " + id, e);
    }
  }

  @Override
  public List<Advertisement> findByAuthorId(UUID authorId) {
    String sql = "SELECT * FROM advertisements WHERE author = ? ORDER BY created_at DESC";

    List<Advertisement> advertisements = new ArrayList<>();

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setObject(1, authorId);
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        Advertisement advertisement = mapResultSetToAdvertisement(resultSet);
        loadPhotoUrls(advertisement, connection);
        advertisements.add(advertisement);
      }

      return advertisements;

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find advertisements by author: " + authorId, e);
    }
  }

  @Override
  public List<Advertisement> findByStatus(AdvertisementStatus status) {
    String sql = "SELECT * FROM advertisements WHERE status = ? ORDER BY created_at DESC";

    List<Advertisement> advertisements = new ArrayList<>();

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, status.name());
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        Advertisement advertisement = mapResultSetToAdvertisement(resultSet);
        loadPhotoUrls(advertisement, connection);
        advertisements.add(advertisement);
      }

      return advertisements;

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find advertisements by status: " + status, e);
    }
  }

  @Override
  public List<Advertisement> findFavorites() {
    String sql = "SELECT * FROM advertisements WHERE is_favorite = TRUE ORDER BY created_at DESC";

    List<Advertisement> advertisements = new ArrayList<>();

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        Advertisement advertisement = mapResultSetToAdvertisement(resultSet);
        loadPhotoUrls(advertisement, connection);
        advertisements.add(advertisement);
      }

      return advertisements;

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find favorite advertisements", e);
    }
  }

  @Override
  public List<Advertisement> findByCategory(Category category) {
    String sql = "SELECT * FROM advertisements WHERE category = ? ORDER BY created_at DESC";

    List<Advertisement> advertisements = new ArrayList<>();

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, category.name()); // Теперь точное совпадение
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        Advertisement advertisement = mapResultSetToAdvertisement(resultSet);
        loadPhotoUrls(advertisement, connection);
        advertisements.add(advertisement);
      }

      return advertisements;

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find advertisements by category: " + category, e);
    }
  }

  @Override
  public Set<Category> getAllCategories() {
    String sql = "SELECT DISTINCT category FROM advertisements WHERE category IS NOT NULL";

    Set<Category> categories = new TreeSet<>(Comparator.comparing(Category::getDisplayName));

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        String categoryStr = resultSet.getString("category").trim();
        if (!categoryStr.isEmpty()) {
          Category category = Category.fromNameSafe(categoryStr);
          if (category != null) {
            categories.add(category);
          }
        }
      }

      return categories;

    } catch (SQLException e) {
      throw new RuntimeException("Failed to get all categories", e);
    }
  }

  @Override
  public Advertisement setCategory(UUID advertisementId, Category category) {
    System.out.println("🔍 DEBUG: Setting single category...");
    System.out.println("🔍 DEBUG: advertisementId: " + advertisementId);
    System.out.println("🔍 DEBUG: category: " + category);

    String sql = "UPDATE advertisements SET category = ? WHERE id = ?";

    try (Connection connection = DatabaseConfig.getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, category.name());
        statement.setObject(2, advertisementId);

        int affectedRows = statement.executeUpdate();

        if (affectedRows == 0) {
          throw new IllegalArgumentException("Advertisement not found: " + advertisementId);
        }
      }

      return findById(advertisementId).orElseThrow(() ->
          new RuntimeException("Advertisement not found after setting category: " + advertisementId)
      );

    } catch (SQLException e) {
      throw new RuntimeException("Failed to set category for advertisement: " + advertisementId, e);
    }
  }

  @Override
  public Category getCategory(UUID advertisementId) {
    String sql = "SELECT category FROM advertisements WHERE id = ?";

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setObject(1, advertisementId);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        String categoryStr = resultSet.getString("category");
        return Category.fromNameSafe(categoryStr);
      } else {
        throw new IllegalArgumentException("Advertisement not found: " + advertisementId);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Failed to get category for advertisement: " + advertisementId, e);
    }
  }

  @Override
  public Advertisement update(Advertisement advertisement) {
    System.out.println("🔍 DEBUG: Starting update for ad: " + advertisement.getId());

    try (Connection connection = DatabaseConfig.getConnection()) {
      connection.setAutoCommit(false);

      try {
        loadPhotoUrls(advertisement, connection);

        // Обновляем основные поля
        String sql = "UPDATE advertisements SET status = ?, author = ?, type = ?, " +
            "category = ?, name = ?, price = ?, description = ?, is_favorite = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
          statement.setString(1, advertisement.getStatus().name());
          statement.setObject(2, advertisement.getAuthorId());
          statement.setString(3, advertisement.getType().name());

          // Категория теперь одно значение
          statement.setString(4, advertisement.getCategory().name());

          statement.setString(5, advertisement.getName());

          if (advertisement.getPrice() != null) {
            statement.setLong(6, advertisement.getPrice());
          } else {
            statement.setNull(6, Types.BIGINT);
          }

          statement.setString(7, advertisement.getDescription());
          statement.setBoolean(8, advertisement.isFavorite());
          statement.setObject(9, advertisement.getId());

          int rowsUpdated = statement.executeUpdate();
          System.out.println("🔍 DEBUG: Rows updated in advertisements: " + rowsUpdated);

          if (rowsUpdated == 0) {
            throw new IllegalArgumentException("Advertisement not found: " + advertisement.getId());
          }
        }

        updatePhotoUrls(advertisement, connection);
        connection.commit();

        return findById(advertisement.getId()).orElseThrow(() ->
            new RuntimeException(
                "Failed to retrieve updated advertisement: " + advertisement.getId())
        );

      } catch (SQLException e) {
        connection.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update advertisement: " + advertisement.getId(), e);
    }
  }

  @Override
  public void delete(UUID id) throws SQLException {
    try (Connection connection = DatabaseConfig.getConnection()) {
      connection.setAutoCommit(false);

      try {
        deletePhotoUrls(id, connection);

        String sql = "DELETE FROM advertisements WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
          statement.setObject(1, id);
          statement.executeUpdate();
        }

        connection.commit();

      } catch (SQLException e) {
        connection.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to delete advertisement: " + id, e);
    }
  }

  @Override
  public Advertisement addPhotoUrl(UUID advertisementId, String photoUrl) {
    System.out.println("🔍 DEBUG: Starting addPhotoUrl...");
    System.out.println("🔍 DEBUG: advertisementId: " + advertisementId);
    System.out.println("🔍 DEBUG: photoUrl: " + photoUrl);

    String getMaxOrderSql = "SELECT COALESCE(MAX(display_order), -1) + 1 as next_order " +
        "FROM advertisement_photos WHERE advertisement_id = ?";
    String insertSql = "INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES (?, ?, ?)";

    try (Connection connection = DatabaseConfig.getConnection()) {
      connection.setAutoCommit(false);

      System.out.println("🔍 DEBUG: Got database connection");

      try {
        int nextOrder;
        try (PreparedStatement getOrderStmt = connection.prepareStatement(getMaxOrderSql)) {
          getOrderStmt.setObject(1, advertisementId);
          System.out.println("🔍 DEBUG: Executing query for max order: " + getMaxOrderSql);
          ResultSet rs = getOrderStmt.executeQuery();
          rs.next();
          nextOrder = rs.getInt("next_order");
          System.out.println("🔍 DEBUG: nextOrder: " + nextOrder);
        }

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
          insertStmt.setObject(1, advertisementId);
          insertStmt.setString(2, photoUrl);
          insertStmt.setInt(3, nextOrder);
          System.out.println("🔍 DEBUG: Executing insert: " + insertSql);
          int rows = insertStmt.executeUpdate();
          System.out.println("🔍 DEBUG: Rows inserted: " + rows);
        }

        connection.commit();
        System.out.println("🔍 DEBUG: Transaction committed successfully");

        // Возвращаем обновленное объявление
        return findById(advertisementId).orElseThrow(() ->
            new RuntimeException("Advertisement not found after adding photo: " + advertisementId)
        );

      } catch (SQLException e) {
        System.err.println("❌ ERROR in addPhotoUrl transaction: " + e.getMessage());
        connection.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to add photo to advertisement: " + advertisementId, e);
    }
  }

  @Override
  public Advertisement removePhotoUrl(UUID advertisementId, String photoUrl) {
    System.out.println("🔍 DEBUG: Starting removePhotoUrl...");
    System.out.println("🔍 DEBUG: advertisementId: " + advertisementId);
    System.out.println("🔍 DEBUG: photoUrl: " + photoUrl);

    String sql = "DELETE FROM advertisement_photos WHERE advertisement_id = ? AND photo_url = ?";

    try (Connection connection = DatabaseConfig.getConnection()) {
      connection.setAutoCommit(false);

      try {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
          statement.setObject(1, advertisementId);
          statement.setString(2, photoUrl);

          System.out.println("🔍 DEBUG: Executing delete: " + sql);
          int affectedRows = statement.executeUpdate();
          System.out.println("🔍 DEBUG: Rows deleted: " + affectedRows);

          if (affectedRows == 0) {
            throw new IllegalArgumentException("Photo not found: " + photoUrl);
          }
        }

        // Переупорядочиваем оставшиеся фото
        reorderPhotos(advertisementId, connection);

        connection.commit();
        System.out.println("🔍 DEBUG: Transaction committed successfully");

        // Возвращаем обновленное объявление
        return findById(advertisementId).orElseThrow(() ->
            new RuntimeException("Advertisement not found after removing photo: " + advertisementId)
        );

      } catch (SQLException e) {
        System.err.println("❌ ERROR in removePhotoUrl transaction: " + e.getMessage());
        connection.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to remove photo: " + photoUrl, e);
    }
  }

  @Override
  public Advertisement updatePrice(UUID advertisementId, Long price) {
    System.out.println("🔍 DEBUG: Starting updatePrice...");
    System.out.println("🔍 DEBUG: advertisementId: " + advertisementId);
    System.out.println("🔍 DEBUG: price: " + price);

    String sql = "UPDATE advertisements SET price = ? WHERE id = ?";

    try (Connection connection = DatabaseConfig.getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        if (price != null) {
          statement.setLong(1, price);
        } else {
          statement.setNull(1, Types.BIGINT);
        }
        statement.setObject(2, advertisementId);

        System.out.println("🔍 DEBUG: Executing update: " + sql);
        int affectedRows = statement.executeUpdate();
        System.out.println("🔍 DEBUG: Rows updated: " + affectedRows);

        if (affectedRows == 0) {
          throw new IllegalArgumentException("Advertisement not found: " + advertisementId);
        }
      }

      // Возвращаем обновленное объявление
      return findById(advertisementId).orElseThrow(() ->
          new RuntimeException("Advertisement not found after updating price: " + advertisementId)
      );

    } catch (SQLException e) {
      throw new RuntimeException("Failed to update price for advertisement: " + advertisementId, e);
    }
  }

  @Override
  public Advertisement toggleFavorite(UUID advertisementId) {
    System.out.println("🔍 DEBUG: Starting toggleFavorite...");
    System.out.println("🔍 DEBUG: advertisementId: " + advertisementId);

    String sql = "UPDATE advertisements SET is_favorite = NOT is_favorite WHERE id = ?";

    try (Connection connection = DatabaseConfig.getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setObject(1, advertisementId);

        System.out.println("🔍 DEBUG: Executing update: " + sql);
        int affectedRows = statement.executeUpdate();
        System.out.println("🔍 DEBUG: Rows updated: " + affectedRows);

        if (affectedRows == 0) {
          throw new IllegalArgumentException("Advertisement not found: " + advertisementId);
        }
      }

      // Возвращаем обновленное объявление
      return findById(advertisementId).orElseThrow(() ->
          new RuntimeException(
              "Advertisement not found after toggling favorite: " + advertisementId)
      );

    } catch (SQLException e) {
      throw new RuntimeException("Failed to toggle favorite for advertisement: " + advertisementId,
          e);
    }
  }

  @Override
  public Advertisement setFavorite(UUID advertisementId, boolean isFavorite) {
    System.out.println("🔍 DEBUG: Starting setFavorite...");
    System.out.println("🔍 DEBUG: advertisementId: " + advertisementId);
    System.out.println("🔍 DEBUG: isFavorite: " + isFavorite);

    String sql = "UPDATE advertisements SET is_favorite = ? WHERE id = ?";

    try (Connection connection = DatabaseConfig.getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setBoolean(1, isFavorite);
        statement.setObject(2, advertisementId);

        System.out.println("🔍 DEBUG: Executing update: " + sql);
        int affectedRows = statement.executeUpdate();
        System.out.println("🔍 DEBUG: Rows updated: " + affectedRows);

        if (affectedRows == 0) {
          throw new IllegalArgumentException("Advertisement not found: " + advertisementId);
        }
      }

      // Возвращаем обновленное объявление
      return findById(advertisementId).orElseThrow(() ->
          new RuntimeException("Advertisement not found after setting favorite: " + advertisementId)
      );

    } catch (SQLException e) {
      throw new RuntimeException("Failed to set favorite for advertisement: " + advertisementId, e);
    }
  }


  @Override
  public void clear() {
    System.out.println("🧹 Очистка базы данных...");

    try (Connection connection = DatabaseConfig.getConnection()) {
      connection.setAutoCommit(true); // Включаем авто-коммит

      try (Statement statement = connection.createStatement()) {
        // Пытаемся очистить таблицу photos (если она существует)
        try {
          String deletePhotosSql = "DELETE FROM advertisement_photos";
          int photosDeleted = statement.executeUpdate(deletePhotosSql);
          System.out.println("🗑️ Удалено " + photosDeleted + " строк из: advertisement_photos");
        } catch (SQLException e) {
          System.out.println(
              "ℹ️ Таблица advertisement_photos не существует или уже пуста: " + e.getMessage());
        }

        // Пытаемся очистить таблицу advertisements (если она существует)
        try {
          String deleteAdsSql = "DELETE FROM advertisements";
          int adsDeleted = statement.executeUpdate(deleteAdsSql);
          System.out.println("🗑️ Удалено " + adsDeleted + " строк из: advertisements");
        } catch (SQLException e) {
          System.out.println(
              "ℹ️ Таблица advertisements не существует или уже пуста: " + e.getMessage());
        }
      }

      System.out.println("✅ База данных полностью очищена!");

    } catch (SQLException e) {
      System.err.println("❌ Ошибка подключения к БД при очистке: " + e.getMessage());
      throw new RuntimeException("Не удалось очистить таблицы", e);
    }
  }

  // Вспомогательные методы

  private void savePhotoUrls(Advertisement advertisement, Connection connection)
      throws SQLException {
    String sql = "INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES (?, ?, ?)";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      int order = 0;
      for (String photoUrl : advertisement.getPhotoUrls()) {
        statement.setObject(1, advertisement.getId());
        statement.setString(2, photoUrl);
        statement.setInt(3, order);
        statement.addBatch();
        order++;
      }
      statement.executeBatch();
    }
  }

  private void loadPhotoUrls(Advertisement advertisement, Connection connection)
      throws SQLException {
    String sql = "SELECT photo_url FROM advertisement_photos WHERE advertisement_id = ? ORDER BY display_order";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, advertisement.getId());
      ResultSet resultSet = statement.executeQuery();

      advertisement.getPhotoUrls().clear();
      while (resultSet.next()) {
        advertisement.getPhotoUrls().add(resultSet.getString("photo_url"));
      }
    }
  }

  private void updatePhotoUrls(Advertisement advertisement, Connection connection)
      throws SQLException {
    deletePhotoUrls(advertisement.getId(), connection);
    savePhotoUrls(advertisement, connection);
  }

  private void deletePhotoUrls(UUID advertisementId, Connection connection) throws SQLException {
    String sql = "DELETE FROM advertisement_photos WHERE advertisement_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, advertisementId);
      statement.executeUpdate();
    }
  }

  // Метод для переупорядочивания фото после удаления
  private void reorderPhotos(UUID advertisementId, Connection connection) throws SQLException {
    // Простой способ: обновляем порядок фото
    String updateOrderSql =
        "WITH ordered AS ( " +
            "  SELECT id, ROW_NUMBER() OVER (ORDER BY display_order) - 1 as new_order " +
            "  FROM advertisement_photos WHERE advertisement_id = ? " +
            ") " +
            "UPDATE advertisement_photos ap " +
            "SET display_order = o.new_order " +
            "FROM ordered o " +
            "WHERE ap.id = o.id AND ap.advertisement_id = ?";

    try (PreparedStatement stmt = connection.prepareStatement(updateOrderSql)) {
      stmt.setObject(1, advertisementId);
      stmt.setObject(2, advertisementId);
      stmt.executeUpdate();
    }
  }

  // Маппинг ResultSet в Advertisement
  private Advertisement mapResultSetToAdvertisement(ResultSet resultSet) throws SQLException {
    UUID id = UUID.fromString(resultSet.getString("id"));
    AdvertisementStatus status = AdvertisementStatus.valueOf(resultSet.getString("status"));
    UUID authorId = UUID.fromString(resultSet.getString("author"));
    Type type = Type.valueOf(resultSet.getString("type"));

    // Категория теперь одно значение
    Category category = null;
    String categoryStr = resultSet.getString("category");
    if (categoryStr != null && !categoryStr.trim().isEmpty()) {
      category = Category.fromNameSafe(categoryStr);
    }

    String name = resultSet.getString("name");

    Long price = resultSet.getLong("price");
    if (resultSet.wasNull()) {
      price = null;
    }

    String description = resultSet.getString("description");
    boolean isFavorite = resultSet.getBoolean("is_favorite");
    Instant createdAt = resultSet.getTimestamp("created_at").toInstant();

    Advertisement advertisement = new Advertisement(id, type, authorId, name, description,
        createdAt);
    advertisement.setStatus(status);
    advertisement.setPrice(price);
    advertisement.setCategory(category); // Устанавливаем одну категорию
    advertisement.setFavorite(isFavorite);

    return advertisement;
  }

  // Вспомогательные методы для работы с категориями

  private String categoriesToString(List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return "";
    }
    return String.join(",", categories);
  }

  private String categoriesToString(Set<Category> categories) {
    if (categories == null || categories.isEmpty()) {
      return "";
    }
    return categories.stream()
        .map(Category::name)
        .reduce((a, b) -> a + "," + b)
        .orElse("");
  }

  private Set<Category> stringToCategories(String categoriesString) {
    Set<Category> categories = new TreeSet<>();

    if (categoriesString == null || categoriesString.trim().isEmpty()) {
      return categories;
    }

    for (String cat : categoriesString.split(",")) {
      String trimmed = cat.trim();
      if (!trimmed.isEmpty()) {
        try {
          Category category = Category.valueOf(trimmed);
          categories.add(category);
        } catch (IllegalArgumentException e) {
          System.out.println("⚠ Некорректная категория в базе: " + trimmed);
        }
      }
    }

    return categories;
  }
}