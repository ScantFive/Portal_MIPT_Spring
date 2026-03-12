package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.repository.user.UserRepository;
import com.mipt.service.util.PasswordHasher;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthentification implements Authentication {
  private final UserRepository userRepository;

  @Override
  public Boolean logIn(String email, String rawPassword) {
    if (userRepository.existsByEmail(email)) {
      Optional<User> userOpt = userRepository.findByEmail(email);
      return userOpt.map(user -> user.checkPassword(rawPassword)).orElse(false);
    } else {
      return false;
    }

  }

  @Override
  public void forgotPassword(String email, String rawPassword1, String rawPassword2) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    // Блок отправки письма

    // Меняем пароль
    if (userOpt.isEmpty()) {
      System.out.println("Пользователь с email " + email + " не найден");
      return;
    }

    // Проверяем, что пароли совпадают (если нужно)
    if (!rawPassword1.equals(rawPassword2)) {
      System.out.println("Пароли не совпадают");
      return;
    }

    User user = userOpt.get();
    user.setHashedPassword(PasswordHasher.hash(rawPassword1)); // ← безопасная замена пароля
    userRepository.update(user);
    System.out.println("Пароль успешно изменён");
  }
}
