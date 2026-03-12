package com.mipt.repository.wallet;

import com.mipt.config.DatabaseConfig;
import com.mipt.model.wallet.Operation;
import com.mipt.model.wallet.OperationType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class OperationRepository {

  public Operation create(Operation operation) {
    String sql = "INSERT INTO operations (id, client, performer, amount, type, time, title) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, operation.getId());
      pst.setObject(2, operation.getClientId());
      pst.setObject(3, operation.getPerformerId());
      pst.setObject(4, operation.getAmount());
      pst.setObject(5, operation.getType().toString());
      pst.setObject(6, Timestamp.from(operation.getTime()));
      pst.setObject(7, operation.getTitle());
      pst.executeUpdate();
      return operation;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean existsById(UUID id) {
    String sql = "SELECT 1 FROM operations WHERE id = ?";
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

  public Operation findById(UUID id) {
    String sql = "SELECT id, client, performer, amount, type, time, title FROM operations WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      try (ResultSet rs = pst.executeQuery()) {
        if (!rs.next()) {
          throw new RuntimeException("No operation with this ID.");
        }
        return Operation.builder()
            .id((UUID) rs.getObject("id"))
            .clientId((UUID) rs.getObject("client"))
            .performerId((UUID) rs.getObject("performer"))
            .amount(rs.getLong("amount"))
            .type(OperationType.valueOf(rs.getString("type")))
            .time(rs.getTimestamp("time").toInstant())
            .title(rs.getString("title"))
            .build();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Operation> findByParticipantsId(UUID clientId, UUID performerId) {
    String sql = "SELECT id, client, performer, amount, type, time, title FROM operations WHERE client = ? AND performer = ?";
    List<Operation> participantsOperations = new ArrayList<>();
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, clientId);
      pst.setObject(2, performerId);
      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          participantsOperations.add(Operation.builder()
              .id((UUID) rs.getObject("id"))
              .clientId((UUID) rs.getObject("client"))
              .performerId((UUID) rs.getObject("performer"))
              .amount(rs.getLong("amount"))
              .type(OperationType.valueOf(rs.getString("type")))
              .time(rs.getTimestamp("time").toInstant())
              .title(rs.getString("title"))
              .build());
        }
      }
      return participantsOperations;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Operation> findAll() {
    String sql = "SELECT id, client, performer, amount, type, time, title FROM operations";
    List<Operation> all = new ArrayList<>();
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        all.add(Operation.builder()
            .id((UUID) rs.getObject("id"))
            .clientId((UUID) rs.getObject("client"))
            .performerId((UUID) rs.getObject("performer"))
            .amount(rs.getLong("amount"))
            .type(OperationType.valueOf(rs.getString("type")))
            .time(rs.getTimestamp("time").toInstant())
            .title(rs.getString("title"))
            .build());
      }
      return all;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void update(Operation newOperation) {
    String sql = "UPDATE operations SET client = ?, performer = ?, amount = ?, type = ?, time = ?, title = ? WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, newOperation.getClientId());
      pst.setObject(2, newOperation.getPerformerId());
      pst.setObject(3, newOperation.getAmount());
      pst.setObject(4, newOperation.getType());
      pst.setObject(5, Timestamp.from(newOperation.getTime()));
      pst.setObject(6, newOperation.getTitle());
      pst.setObject(7, newOperation.getId());
      int updated = pst.executeUpdate();
      if (updated == 0) {
        create(newOperation);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteById(UUID id) {
    String sql = "DELETE FROM operations WHERE id = ?";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
