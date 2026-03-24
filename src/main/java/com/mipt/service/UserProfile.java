package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.model.wallet.Operation;
import com.mipt.model.wallet.Wallet;
import com.mipt.repository.user.UserRepository;
import com.mipt.repository.wallet.OperationRepository;
import com.mipt.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfile implements Profile {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final OperationRepository operationRepository;
  private final PaymentService paymentService;


  @Override
  @Transactional(readOnly = true)
  public User getInfo(UUID userId) {
    log.debug("Getting user info for ID: {}", userId);

    return userRepository.findById(userId)
      .orElseThrow(() -> {
        log.error("User not found with ID: {}", userId);
        return new RuntimeException("Пользователь не найден");
      });
  }

  @Override
  @Transactional
  public void editLogin(UUID userId, String newLogin) {
    log.info("Editing login for user: {}", userId);

    if (newLogin == null || newLogin.trim().length() < 3) {
      throw new RuntimeException("Логин должен содержать минимум 3 символа");
    }

    if (userRepository.existsByLogin(newLogin)) {
      throw new RuntimeException("Логин уже занят");
    }

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    String oldLogin = user.getLogin();
    user.setLogin(newLogin.trim());
    userRepository.save(user);

    log.info("Login changed from '{}' to '{}' for user: {}", oldLogin, newLogin, userId);
  }

  @Override
  @Transactional
  public void editEmail(UUID userId, String newEmail) {
    log.info("Editing email for user: {}", userId);

    if (newEmail == null || !newEmail.trim().endsWith("@phystech.edu")) {
      throw new RuntimeException("Email должен заканчиваться на @phystech.edu");
    }

    if (userRepository.existsByEmail(newEmail)) {
      throw new RuntimeException("Email уже используется");
    }

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    String oldEmail = user.getEmail();
    user.setEmail(newEmail.trim().toLowerCase());
    userRepository.save(user);

    log.info("Email changed from '{}' to '{}' for user: {}", oldEmail, newEmail, userId);
  }

  @Override
  @Transactional
  public void editPassword(UUID userId, String oldPassword, String newPassword, String confirmPassword) {
    log.info("Changing password for user: {}", userId);

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    if (!user.checkPassword(oldPassword)) {
      log.warn("Invalid old password attempt for user: {}", userId);
      throw new RuntimeException("Неверный текущий пароль");
    }

    if (!newPassword.equals(confirmPassword)) {
      log.warn("New passwords don't match for user: {}", userId);
      throw new RuntimeException("Новые пароли не совпадают");
    }

    if (newPassword.length() < 6) {
      throw new RuntimeException("Пароль должен содержать минимум 6 символов");
    }

    user.changePassword(newPassword, confirmPassword);
    userRepository.save(user);

    log.info("Password successfully changed for user: {}", userId);
  }

  @Override
  @Transactional(readOnly = true)
  public long getTokensAmount(UUID userId) {
    log.debug("Getting token amount for user: {}", userId);

    Wallet wallet = walletRepository.findByUserId(userId)
      .orElseThrow(() -> {
        log.error("Wallet not found for user: {}", userId);
        return new RuntimeException("Кошелек не найден");
      });

    return wallet.getAvailableTokens();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Operation> dealHistory(UUID userId) {
    log.debug("Getting deal history for user: {}", userId);

    Wallet wallet = walletRepository.findByUserId(userId)
      .orElseThrow(() -> {
        log.error("Wallet not found for user: {}", userId);
        return new RuntimeException("Кошелек не найден");
      });

    return paymentService.findAllOperationsByWalletId(wallet.getId());
  }

  @Override
  public void logOut() {
    log.info("User logged out");
  }
}
