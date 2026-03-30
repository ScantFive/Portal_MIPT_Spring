package com.mipt.chat.repository;

import com.mipt.chat.model.Message;
import com.mipt.util.DatabaseConfig;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageRepository {

  public Message create(Message message) {
    String sql = "INSERT INTO messages (id, chat, sender, text, sending_time, editing_time, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, message.getId());
      pst.setObject(2, message.getChatId());
      pst.setObject(3, message.getSenderId());
      pst.setObject(4, message.getText());
      pst.setObject(5, Timestamp.from(message.getSendingTime()));
      pst.setObject(6, Timestamp.from(message.getEditingTime()));
      pst.setObject(7, message.isRead());
      pst.executeUpdate();
      return message;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean existsById(UUID id) {
    String sql = "SELECT 1 FROM messages WHERE id = ?";
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

  public Message findById(UUID id) {
    String sql = "SELECT id, chat, sender, text, sending_time, editing_time, is_read FROM messages WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      try (ResultSet rs = pst.executeQuery()) {
        if (!rs.next()) {
          throw new RuntimeException("No message with this ID.");
        }
        return Message.builder()
            .id((UUID) rs.getObject("id"))
            .chatId((UUID) rs.getObject("chat"))
            .senderId((UUID) rs.getObject("sender"))
            .text(rs.getString("text"))
            .sendingTime(rs.getTimestamp("sending_time").toInstant())
            .editingTime(rs.getTimestamp("editing_time").toInstant())
            .isRead(rs.getBoolean("is_read"))
            .build();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Message> findByChatId(UUID chatId) {
    String sql = "SELECT id, chat, sender, text, sending_time, editing_time, is_read FROM messages WHERE chat = ?";
    List<Message> result = new ArrayList<>();
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, chatId);
      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          result.add(Message.builder()
              .id((UUID) rs.getObject("id"))
              .chatId((UUID) rs.getObject("chat"))
              .senderId((UUID) rs.getObject("sender"))
              .text(rs.getString("text"))
              .sendingTime(rs.getTimestamp("sending_time").toInstant())
              .editingTime(rs.getTimestamp("editing_time").toInstant())
              .isRead(rs.getBoolean("is_read"))
              .build());
        }
        return result;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Message> findAll() {
    String sql = "SELECT id, chat, sender, text, sending_time, editing_time, is_read FROM messages";
    List<Message> all = new ArrayList<>();
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        all.add(Message.builder()
            .id((UUID) rs.getObject("id"))
            .chatId((UUID) rs.getObject("chat"))
            .senderId((UUID) rs.getObject("sender"))
            .text(rs.getString("text"))
            .sendingTime(rs.getTimestamp("sending_time").toInstant())
            .editingTime(rs.getTimestamp("editing_time").toInstant())
            .isRead(rs.getBoolean("is_read"))
            .build());
      }
      return all;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void update(Message newMessage) {
    String sql = "UPDATE messages SET chat = ?, sender = ?, text = ?, sending_time = ?, editing_time = ?, is_read = ? WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, newMessage.getChatId());
      pst.setObject(2, newMessage.getSenderId());
      pst.setObject(3, newMessage.getText());
      pst.setObject(4, Timestamp.from(newMessage.getSendingTime()));
      pst.setObject(5, Timestamp.from(newMessage.getEditingTime()));
      pst.setObject(6, newMessage.isRead());
      pst.setObject(7, newMessage.getId());
      int updated = pst.executeUpdate();
      if (updated == 0) {
        create(newMessage);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteById(UUID id) {
    String sql = "DELETE FROM messages WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteByChatId(UUID chatId) {
    String sql = "DELETE FROM messages WHERE chat = ?";
    try (var conn = DatabaseConfig.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, chatId);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
