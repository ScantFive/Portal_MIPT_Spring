package com.mipt.user.service;

import com.mipt.user.model.User;
import com.mipt.user.repository.UserRepository;
import com.mipt.wallet.repository.WalletRepository;

import java.util.Optional;
import lombok.Data;

@Data
public class UserRegistration implements Registration {
  private UserRepository userRepository;
  private WalletRepository walletRepository = new WalletRepository();

  public UserRegistration(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void register(String login, String email, String password) {

    if (!email.trim().endsWith("@phystech.edu")) {
      System.out.println("Email не подходит по формату");
      return;
    }

    if (userRepository.existsByEmail(email)) {
      System.out.println("Пользователь с таким email уже существует");
      return;
    }

    if (login.trim().length() < 3) {
      System.out.println("Login слишком короткий");
      return;
    }

    if (password.trim().length() < 6) {
      System.out.println("Пароль слишком короткий");
      return;
    }
    User user = new User(login, email, password);
    userRepository.save(user);
    walletRepository.createWithUserId(user.getUserID());
    System.out.println("Пользователь " + login + " успешно зарегистрирован!");
  }

  @Override
  public void verification(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      user.setActivated(true);
      userRepository.update(user);
      System.out.println("Пользователь " + email + " активирован");
    } else {
      System.out.println("Пользователь с email " + email + " не найден");
    }
  }

  @Override
  public void toProfile() {
    System.out.println("Redirecting to user profile...");
  }
}
