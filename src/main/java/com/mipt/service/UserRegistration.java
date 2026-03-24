package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.repository.user.UserRepository;
import com.mipt.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistration implements Registration {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;

  @Override
  public void register(String login, String email, String password) {
    log.info("Attempting to register new user: {}", login);

    if (!email.trim().endsWith("@phystech.edu")) {
      log.warn("Registration failed - invalid email domain: {}", email);
      throw new RuntimeException("Email должен заканчиваться на @phystech.edu");
    }

    if (userRepository.existsByEmail(email)) {
      log.warn("Registration failed - email already exists: {}", email);
      throw new RuntimeException("Пользователь с таким email уже существует");
    }

    if (login.trim().length() < 3) {
      log.warn("Registration failed - login too short: {}", login);
      throw new RuntimeException("Логин должен содержать минимум 3 символа");
    }

    if (password.trim().length() < 6) {
      log.warn("Registration failed - password too short for: {}", login);
      throw new RuntimeException("Пароль должен содержать минимум 6 символов");
    }

    User user = new User(login, email, password);
    User savedUser = userRepository.save(user);
    log.debug("User saved with ID: {}", savedUser.getUserId());

    try {
      walletRepository.createWithUserId(savedUser.getUserId());
      log.info("Wallet created for user: {}", savedUser.getUserId());
    } catch (Exception e) {
      log.error("Failed to create wallet for user: {}", savedUser.getUserId(), e);
      throw new RuntimeException("Не удалось создать кошелек для пользователя", e);
    }

    log.info("User successfully registered: {}", login);
    System.out.println("Пользователь " + login + " успешно зарегистрирован!");
  }

  @Override
  @Transactional
  public void verification(String email) {
    log.info("Verifying user: {}", email);

    Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());

    if (userOpt.isPresent()) {
      User user = userOpt.get();

      if (user.getActivated()) {
        log.info("User already activated: {}", email);
        System.out.println("Пользователь " + email + " уже активирован");
        return;
      }

      user.setActivated(true);
      userRepository.save(user);

      log.info("User successfully activated: {}", email);
      System.out.println("Пользователь " + email + " активирован");
    } else {
      log.warn("Verification failed - user not found: {}", email);
      System.out.println("Пользователь с email " + email + " не найден");
      throw new RuntimeException("Пользователь не найден");
    }
  }

  @Override
  public void toProfile() {
    log.debug("Redirecting to user profile");
    System.out.println("Redirecting to user profile...");
  }
}
