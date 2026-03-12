package com.mipt.repository.user;

import com.mipt.model.user.User;
import com.mipt.config.DatabaseConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
@Transactional
public class UserRepository {

  public void save(User user) {
    String sql = """
        INSERT INTO users (id, login, email, hashed_password, activated)
        VALUES (?, ?, ?, ?, ?)
        """;
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, user.getUserID());
      stmt.setString(2, user.getLogin());
      stmt.setString(3, user.getEmail().toLowerCase().trim());
      stmt.setString(4, user.getHashedPassword());
      stmt.setBoolean(5, user.getActivated());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка сохранения пользователя", e);
    }
  }

  public boolean existsByEmail(String email) {
    String sql = "SELECT 1 FROM users WHERE email = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, email.toLowerCase().trim());
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка проверки email", e);
    }
  }

  public Optional<User> findByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, email.toLowerCase().trim());
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToUser(rs));
        }
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка поиска по email", e);
    }
  }

  public Optional<User> findById(UUID id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToUser(rs));
        }
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка поиска по ID", e);
    }
  }

  public void update(User user) {
    String sql = """
        UPDATE users
        SET login = ?, email = ?, hashed_password = ?, activated = ?
        WHERE id = ?
        """;
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, user.getLogin());
      stmt.setString(2, user.getEmail().toLowerCase().trim());
      stmt.setString(3, user.getHashedPassword());
      stmt.setBoolean(4, user.getActivated());
      stmt.setObject(5, user.getUserID());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка обновления пользователя", e);
    }
  }

  public List<User> findAll() {
    String sql = "SELECT * FROM users";
    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      List<User> users = new ArrayList<>();
      while (rs.next()) {
        users.add(mapRowToUser(rs));
      }
      return users;
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения всех пользователей", e);
    }
  }

  public boolean deleteById(UUID id) {
    String sql = "DELETE FROM users WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, id);
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка удаления по ID: " + id, e);
    }
  }

  public void clear() {
    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM users");
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка очистки таблицы", e);
    }
  }

  // Вспомогательный метод: ResultSet → User
  private User mapRowToUser(ResultSet rs) throws SQLException {
    UUID id = (UUID) rs.getObject("id");
    String login = rs.getString("login");
    String email = rs.getString("email");
    String passwordHash = rs.getString("hashed_password");
    boolean activated = rs.getBoolean("activated");

    return User.fromDatabase(id, login, email, passwordHash, activated);
  }
}
