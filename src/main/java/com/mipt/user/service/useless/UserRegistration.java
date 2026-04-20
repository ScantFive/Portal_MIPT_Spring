package com.mipt.user.service.useless;

import com.mipt.user.model.User;
import com.mipt.user.repository.useless.UserRepository;
import com.mipt.wallet.repository.WalletRepository;

import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class UserRegistration implements Registration {
  private UserRepository userRepository;
  private WalletRepository walletRepository = new WalletRepository();

  public UserRegistration(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void register(User user) {
    String email = user.getEmail();
    String login = user.getLogin();
    String password = user.getHashedPassword();
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
    userRepository.save(user);
    walletRepository.createWithUserId(user.getUserID());
      log.info("Пользователь {} успешно зарегистрирован!", login);
  }

  @Override
  public void verification(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      user.setActivated(true);
      userRepository.update(user);
        log.info("Пользователь {} активирован", email);
    } else {
        log.info("Пользователь с email {} не найден", email);
    }
  }

  @Override
  public void toProfile() {
    log.info("Redirecting to user profile...");
  }
}
