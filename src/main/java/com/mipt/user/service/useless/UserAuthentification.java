package com.mipt.user.service.useless;

import com.mipt.user.model.User;
import com.mipt.user.repository.useless.UserRepository;
import com.mipt.util.PasswordHasher;
import java.util.*;

public class UserAuthentification implements Authentication {
  private final UserRepository userRepository;

  public UserAuthentification(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

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
