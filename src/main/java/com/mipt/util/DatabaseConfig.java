package com.mipt.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class DatabaseConfig {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseConfig(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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
          System.err.println("Skipping statement due to error: " + e.getMessage());
        }
      }
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void initializeDatabase() {
    try {
      executeInitScripts();
    } catch (Exception e) {
      System.err.println("Database initialization skipped: " + e.getMessage());
    }
  }

  public void recreateAllTables() {
    try {
      executeInitScripts();
    } catch (Exception e) {
      throw new RuntimeException("Failed to recreate database tables", e);
    }
  }

  private void executeInitScripts() throws IOException {
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

    for (String script : scripts) {
      executeSqlScriptFromResource(script);
    }
  }
}
