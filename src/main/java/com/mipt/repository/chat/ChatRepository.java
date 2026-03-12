package com.mipt.repository.chat;

import com.mipt.model.chat.Chat;
import com.mipt.config.DatabaseConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRepository { 

  public Chat create(Chat chat) {
    String sql = "INSERT INTO chats (id, owner, member, last_update) VALUES (?, ?, ?, ?)";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, chat.getId());
      pst.setObject(2, chat.getOwnerId());
      pst.setObject(3, chat.getMemberId());
      pst.setObject(4, Timestamp.from(chat.getLastUpdate()));
      pst.executeUpdate();
      return chat;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean existsById(UUID id) {
    String sql = "SELECT 1 FROM chats WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      try (ResultSet rs = pst.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Chat findById(UUID id) {
    String sql = "SELECT id, owner, member, last_update FROM chats WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      try (ResultSet rs = pst.executeQuery()) {
        if (!rs.next()) {
          throw new RuntimeException("No chat with this ID.");
        }
        return Chat.builder()
            .id((UUID) rs.getObject("id"))
            .ownerId((UUID) rs.getObject("owner"))
            .memberId((UUID) rs.getObject("member"))
            .lastUpdate(rs.getTimestamp("last_update").toInstant())
            .build();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Chat findByOwnerAndMemberId(UUID ownerId, UUID memberId) {
    String sql = "SELECT id, owner, member, last_update FROM chats WHERE owner = ? AND member = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, ownerId);
      pst.setObject(2, memberId);
      try (ResultSet rs = pst.executeQuery()) {
        if (!rs.next()) {
          throw new RuntimeException("No chat with this ID.");
        }
        return Chat.builder()
            .id((UUID) rs.getObject("id"))
            .ownerId((UUID) rs.getObject("owner"))
            .memberId((UUID) rs.getObject("member"))
            .lastUpdate(rs.getTimestamp("last_update").toInstant())
            .build();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Chat> findAll() {
    String sql = "SELECT id, owner, member, last_update FROM chats";
    List<Chat> all = new ArrayList<>();
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        all.add(Chat.builder()
            .id((UUID) rs.getObject("id"))
            .ownerId((UUID) rs.getObject("owner"))
            .memberId((UUID) rs.getObject("member"))
            .lastUpdate(rs.getTimestamp("last_update").toInstant())
            .build());
      }
      return all;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void update(Chat newChat) {
    String sql = "UPDATE chats SET owner = ?, member = ?, last_update = ? WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, newChat.getOwnerId());
      pst.setObject(2, newChat.getMemberId());
      pst.setObject(3, Timestamp.from(newChat.getLastUpdate()));
      pst.setObject(4, newChat.getId());
      int updated = pst.executeUpdate();
      if (updated == 0) {
        create(newChat);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteById(UUID id) {
    String sql = "DELETE FROM chats WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
