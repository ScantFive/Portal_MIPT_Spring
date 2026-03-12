package com.mipt.repository.wallet;

import com.mipt.config.DatabaseConfig;
import com.mipt.model.wallet.Wallet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class WalletRepository {

  public Wallet create(Wallet wallet) {
    String sql = "INSERT INTO wallets (owner, available_tokens, reserved_tokens) VALUES (?, ?, ?)";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, wallet.getOwnerId());
      pst.setObject(2, wallet.getAvailableTokens());
      pst.setObject(3, wallet.getReservedTokens());
      pst.executeUpdate();
      return wallet;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Wallet createWithUserId(UUID ownerId) {
    Wallet wallet = Wallet.builder().ownerId(ownerId).build();
    return create(wallet);
  }

  public boolean existsById(UUID id) {
    String sql = "SELECT 1 FROM wallets WHERE owner = ?";
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

  public Wallet findById(UUID ownerId) {
    String sql = "SELECT owner, available_tokens, reserved_tokens FROM wallets WHERE owner = ?";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, ownerId);
      try (ResultSet rs = pst.executeQuery()) {
        if (!rs.next()) {
          throw new RuntimeException("No wallet with this owner ID.");
        }
        return Wallet.builder()
            .ownerId((UUID) rs.getObject("owner"))
            .availableTokens(rs.getLong("available_tokens"))
            .reservedTokens(rs.getLong("reserved_tokens"))
            .build();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Wallet> findAll() {
    String sql = "SELECT owner, available_tokens, reserved_tokens FROM wallets";
    List<Wallet> all = new ArrayList<>();
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        all.add(Wallet.builder()
            .ownerId((UUID) rs.getObject("owner"))
            .availableTokens(rs.getLong("available_tokens"))
            .reservedTokens(rs.getLong("reserved_tokens"))
            .build());
      }
      return all;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void update(Wallet newWallet) {
    String sql = "UPDATE wallets SET available_tokens = ?, reserved_tokens = ? WHERE owner = ?";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, newWallet.getAvailableTokens());
      pst.setObject(2, newWallet.getReservedTokens());
      pst.setObject(3, newWallet.getOwnerId());
      int updated = pst.executeUpdate();
      if (updated == 0) {
        create(newWallet);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteById(UUID ownerId) {
    String sql = "DELETE FROM wallets WHERE owner = ?";
    try (var conn = DatabaseConfig.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setObject(1, ownerId);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
