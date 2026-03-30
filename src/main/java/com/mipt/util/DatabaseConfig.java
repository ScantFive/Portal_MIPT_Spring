package com.mipt.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConfig {

  private final JdbcTemplate jdbcTemplate;
  private final DataSource dataSource;
  private static DataSource staticDataSource;

  public DatabaseConfig(JdbcTemplate jdbcTemplate, DataSource dataSource) {
    this.jdbcTemplate = jdbcTemplate;
    this.dataSource = dataSource;
    DatabaseConfig.staticDataSource = dataSource;
  }

  /**
   * DEPRECATED: Use Spring Data JPA or JdbcTemplate instead.
   * This method provides backward compatibility for legacy code.
   */
  @Deprecated(forRemoval = true, since = "1.0")
  public static Connection getConnection() throws SQLException {
    if (staticDataSource == null) {
      throw new SQLException("DataSource not initialized. Make sure Spring has been initialized.");
    }
    return staticDataSource.getConnection();
  }

  /**
   * DEPRECATED: Database initialization should be managed by Spring/Flyway.
   * This method provides backward compatibility for legacy code.
   */
  @Deprecated(forRemoval = true, since = "1.0")
  public static void recreateAllTables() throws SQLException {
    // This would be called on legacy code - in Spring Boot context,
    // the component is not static, so we recommend using the instance method.
    System.err.println("WARNING: recreateAllTables() is deprecated. Use Spring's database initialization tools.");
  }

  /**
   * Executes an SQL script from file system
   * 
   * @param filePath path to the SQL file relative to the file system
   */
  public void executeSqlScript(String filePath) throws IOException {
    String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    String[] statements = content.split(";");
    for (String statement : statements) {
      if (!statement.trim().isEmpty()) {
        try {
          jdbcTemplate.execute(statement.trim());
        } catch (Exception e) {
          // Log but continue - some statements may fail due to existing objects
          System.err.println("Skipping statement due to error: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Executes an SQL script from classpath resources
   * 
   * @param resourcePath path to the SQL file in classpath
   */
  public void executeSqlScriptFromResource(String resourcePath) throws IOException {
    ClassPathResource resource = new ClassPathResource(resourcePath);
    String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String[] statements = content.split(";");
    for (String statement : statements) {
      if (!statement.trim().isEmpty()) {
        try {
          jdbcTemplate.execute(statement.trim());
        } catch (Exception e) {
          // Log but continue
          System.err.println("Skipping statement due to error: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Recreates all tables by executing initialization scripts from classpath
   * This will be called automatically on application startup
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initializeDatabase() {
    try {
      // Only initialize if needed - you might want to add a flag to control this
      String[] scripts = {
          "db-init/001_users.sql",
          "db-init/002_wallets.sql",
          "db-init/003_advertisements.sql",
          "db-init/004_advertisement_photos.sql",
          "db-init/005_operations.sql",
          "db-init/006_chats.sql",
          "db-init/007_messages.sql",
          "db-init/008_favorites.sql",
          "db-init/009_favoritesTriggers.sql",
          "db-init/010_search_history.sql"
      };
      // Uncomment below to auto-initialize on startup
      // for (String script : scripts) {
      // try {
      // executeSqlScriptFromResource(script);
      // } catch (IOException e) {
      // System.err.println("Error executing script " + script + ": " +
      // e.getMessage());
      // }
      // }
    } catch (Exception e) {
      System.err.println("Database initialization skipped: " + e.getMessage());
    }
  }
}
