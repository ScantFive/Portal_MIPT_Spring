package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthentification implements Authentification {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public Boolean logIn(String email, String rawPassword) {
    log.debug("Attempting login for email: {}", email);

    Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());

    if (userOpt.isEmpty()) {
      log.warn("Login failed - user not found: {}", email);
      return false;
    }

    User user = userOpt.get();
    boolean passwordMatches = user.checkPassword(rawPassword);

    if (passwordMatches) {
      log.info("Successful login for user: {}", user.getLogin());
    } else {
      log.warn("Failed login attempt - wrong password for: {}", email);
    }

    return passwordMatches;
  }

  @Override
  @Transactional
  public void forgotPassword(String email, String newPassword, String confirmPassword) {
    log.debug("Password reset requested for email: {}", email);

    Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());

    if (userOpt.isEmpty()) {
      log.warn("Password reset failed - user not found: {}", email);
      throw new RuntimeException("Пользователь с email " + email + " не найден");
    }

    if (!newPassword.equals(confirmPassword)) {
      log.warn("Password reset failed - passwords don't match for: {}", email);
      throw new RuntimeException("Пароли не совпадают");
    }

    if (newPassword.length() < 6) {
      log.warn("Password reset failed - password too short for: {}", email);
      throw new RuntimeException("Пароль должен содержать минимум 6 символов");
    }

    User user = userOpt.get();

    user.changePassword(newPassword, confirmPassword);

    userRepository.save(user);


    log.info("Password successfully changed for user: {}", user.getLogin());
    System.out.println("Пароль успешно изменён");
  }

  @Transactional(readOnly = true)
  public boolean userExists(String email) {
    return userRepository.existsByEmail(email.toLowerCase().trim());
  }

  @Transactional(readOnly = true)
  public Optional<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email.toLowerCase().trim());
  }
}
