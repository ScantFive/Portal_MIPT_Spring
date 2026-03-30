package com.mipt.checks;

import com.mipt.user.model.User;
import com.mipt.user.repository.UserRepository;
import com.mipt.user.service.Authentication;
import com.mipt.user.service.UserAuthentification;
import com.mipt.user.service.UserRegistration;
import java.util.List;
import java.util.Optional;

public class RegistrationCheck {
  public static void regCheck() {
    UserRepository userRepository = new UserRepository();
    UserRegistration reg = new UserRegistration(userRepository);
    Authentication auth = new UserAuthentification(userRepository);

    String[] emails = {
      "alice@phystech.edu",
      "bob@phystech.edu",
      "charlie@phystech.edu",
      "diana@phystech.edu",
      "eve@phystech.edu"
    };

    String[] usernames = {
      "alice",
      "bob",
      "charlie",
      "diana",
      "eve"
    };

    String[] passwords = {
      "Pass123!",
      "Pass456!",
      "Pass789!",
      "Pass000!",
      "Pass111!"
    };

    System.out.println("=== Тест: Массовая регистрация пользователей ===");
    for (int i = 0; i < emails.length; i++) {
      System.out.println("\nРегистрация пользователя " + (i + 1) + ": " + usernames[i]);
      reg.register(usernames[i], emails[i], passwords[i]);

      // Проверка, что пользователь действительно сохранён
      Optional<User> maybeUser = userRepository.findByEmail(emails[i]);
      if (maybeUser.isPresent()) {
        System.out.println("  Успешно зарегистрирован: " + maybeUser.get());
      } else {
        System.out.println("  ОШИБКА: Пользователь не найден после регистрации!");
      }
    }

    // Попытка повторной регистрации первого пользователя
    System.out.println("\n=== Тест: Дублирование email (alice@phystech.edu) ===");
    reg.register("alice2", "alice@phystech.edu", "anotherPass");

    // Проверка входа для всех пользователей
    System.out.println("\n=== Тест: Аутентификация всех зарегистрированных пользователей ===");
    for (int i = 0; i < emails.length; i++) {
      boolean loginSuccess = auth.logIn(emails[i], passwords[i]);
      System.out.println("Вход " + usernames[i] + ": " + (loginSuccess ? "УСПЕШЕН" : "ОШИБКА"));
    }

    // Попытка входа с неверным паролем
    System.out.println("\n=== Тест: Неверный пароль для bob ===");
    boolean wrongLogin = auth.logIn("bob@phystech.edu", "wrongPass");
    System.out.println("Вход с неверным паролем: " + (wrongLogin ? "УСПЕШЕН (ошибка!)" : "ОТКЛОНЁН (корректно)"));

    // Попытка сброса пароля (если ваша логика допускает изменение)
    System.out.println("\n=== Тест: Сброс пароля для charlie ===");
    auth.forgotPassword("charlie@phystech.edu", "NewPass999!", "NewPass999!");

    // Проверка входа с новым паролем
    boolean loginAfterReset = auth.logIn("charlie@phystech.edu", "NewPass999!");
    System.out.println("Вход после сброса пароля: " + (loginAfterReset ? "УСПЕШЕН" : "ОШИБКА"));

    // Проверка, что старый пароль больше не работает
    boolean oldPassFails = auth.logIn("charlie@phystech.edu", "Pass789!");
    System.out.println("Вход со старым паролем: " + (oldPassFails ? "УСПЕШЕН (ошибка!)" : "ОТКЛОНЁН (корректно)"));

    // Вывод всех пользователей
    List<User> allUsers = userRepository.findAll();
    System.out.println("\n=== Все пользователи в системе ===");
    allUsers.forEach(System.out::println);

    // Опционально: удаление всех созданных пользователей
    System.out.println("\n=== Удаление всех пользователей ===");
    for (int i = 6; i < allUsers.size(); i++) {
      User u = allUsers.get(i);
      userRepository.deleteById(u.getUserID());
      System.out.println("Удалён: " + u.getEmail());
    }

    System.out.println("\n=== Тест завершён ===");
  }
}
