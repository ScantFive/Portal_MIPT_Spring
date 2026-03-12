package com.mipt.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

  static {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
      "jdbc:postgresql://localhost:5432/portal_DB", "postgres", "JavaMTS");
  }

  public static void executeSqlScript(Connection conn, String filePath)
    throws SQLException, IOException {
    String content = new String(Files.readAllBytes(Path.of(filePath)));

    // Используем try-with-resources для автоматического закрытия Statement
    try (Statement stmt = conn.createStatement()) {
      // Выполняем весь скрипт целиком, чтобы правильно обработать блоки DO $$
      stmt.execute(content);
    }
  }

  public static void recreateAllTables() throws SQLException, IOException {
    // Используем одно соединение для всех скриптов и закрываем его в конце
    try (Connection conn = getConnection()) {
      executeSqlScript(conn, "src/main/resources/db-init/001_users.sql");
      executeSqlScript(conn, "src/main/resources/db-init/002_wallets.sql");
      executeSqlScript(conn, "src/main/resources/db-init/003_advertisements.sql");
      executeSqlScript(conn, "src/main/resources/db-init/004_advertisement_photos.sql");
      executeSqlScript(conn, "src/main/resources/db-init/005_operations.sql");
      executeSqlScript(conn, "src/main/resources/db-init/006_chats.sql");
      executeSqlScript(conn, "src/main/resources/db-init/007_messages.sql");
      executeSqlScript(conn, "src/main/resources/db-init/008_favorites.sql");
      executeSqlScript(conn, "src/main/resources/db-init/009_favoritesTriggers.sql");
      executeSqlScript(conn, "src/main/resources/db-init/010_search_history.sql");
    }
  }
}
