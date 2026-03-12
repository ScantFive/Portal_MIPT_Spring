package com.mipt.service.checks;



import com.mipt.model.user.User;
import com.mipt.repository.user.UserRepository;
import com.mipt.service.UserAuthentification;
import com.mipt.service.UserProfile;
import com.mipt.service.UserRegistration;

import java.util.Scanner;

public class FullRegistrationCheck {

  public static void fullRegCheck() {

    Scanner input = new Scanner(System.in);
    UserRepository userRepository = new UserRepository();
    UserRegistration reg = new UserRegistration(userRepository);
    UserAuthentification auth = new UserAuthentification(userRepository);
    UserProfile prof = null;

    System.out.println("0| Выйти из программы");
    System.out.println("1| Войти в аккаунт");
    System.out.println("2| Забыл пароль");
    System.out.println("3| Показать историю сделок");
    System.out.println("4| Показать информацию профиля");
    System.out.println("5| Изменить логин");
    System.out.println("6| Изменить почту");
    System.out.println("7| Изменить пароль");
    System.out.println("8| Выйти из аккаунта");
    System.out.println("9| Зарегистрировать пользователя");
    System.out.println("all| Вывести всех пользователей");
    System.out.println("help| Вывести реализуемые команды");
    System.out.println("ver| Активировать пользователя");
    System.out.println("del| Удалить пользователя");
    String login;
    String email;
    String password;
    User user;
    User nullUser = new User("123", "123@phystech.edu", "123123");
    User profileUser = null;
    boolean inAccount = false;

    label:
    while (true) {
      System.out.print("Ввод: ");
      String call = input.next();
      switch (call) {
        case "0":
          break label;
        case "help":
          System.out.println("0| Выйти из программы");
          System.out.println("1| Войти в аккаунт");
          System.out.println("2| Забыл пароль");
          System.out.println("3| Показать историю сделок");
          System.out.println("4| Показать информацию профиля");
          System.out.println("5| Изменить логин");
          System.out.println("6| Изменить почту");
          System.out.println("7| Изменить пароль");
          System.out.println("8| Выйти из аккаунта");
          System.out.println("9| Зарегистрировать пользователя");
          System.out.println("all| Вывести всех пользователей");
          System.out.println("help| Вывести реализуемые команды");
          System.out.println("ver| Активировать пользователя");
          System.out.println("del| Удалить пользователя");
          break;
        case "9":
          System.out.print("Login: ");
          login = input.next();
          System.out.print("Email: ");
          email = input.next();
          System.out.print("Password: ");
          password = input.next();
          reg.register(login, email, password);
          break;
        case "ver":
          email = input.next();
          reg.verification(email);
          break;
        case "del":
          email = input.next();
          user = reg.getUserRepository().findByEmail(email).orElse(nullUser);
          if (userRepository.existsByEmail(email)) {
            userRepository.deleteById(user.getUserID());
            System.out.println("Пользователь с почтой " + email + " удалён");
          }
          break;
        case "1":
          System.out.print("Email: ");
          email = input.next();
          System.out.print("Password: ");
          password = input.next();
          inAccount = auth.logIn(email, password);
          if (inAccount && userRepository.findByEmail(email).orElse(nullUser).getActivated()) {
            System.out.println("Вы вошли в аккаунт");
            profileUser = userRepository.findByEmail(email).orElse(nullUser);
            prof = new UserProfile(profileUser.getUserID());
            profileUser = prof.getInfo();
          } else if (inAccount && !userRepository.findByEmail(email).orElse(nullUser).getActivated()) {
            System.out.println("Почта не подтверждена");
          } else {
            System.out.println("Неверная почта или пароль");}
          break;
        case "2":
          System.out.print("Email: ");
          email = input.next();
          System.out.print("NewPassword: ");
          password = input.next();
          auth.forgotPassword(email, password, password);
          break;
        case "3":
          if (inAccount) {
            assert prof != null;
            System.out.println(prof.dealHistory(profileUser.getUserID()));
          } else {
            System.out.println("Пользователь не вошёл в аккаунт");
          }
          break;
        case "4":
          if (inAccount) {
            System.out.println(profileUser);
            assert prof != null;
            System.out.println("Tokens: " + prof.getTokensAmount(profileUser.getUserID()));
          } else {
            System.out.println("Пользователь не вошёл в аккаунт");
          }
          break;
        case "all":
          System.out.println(userRepository.findAll());
          break;
        case "8":
          inAccount = false;
          profileUser = null;
          prof = null;
          System.out.println("Вы вышли из аккаунта");
          break;
        case "6":
          if (inAccount) {
            System.out.print("Новый email: ");
            email = input.next();
            assert prof != null;
            assert profileUser != null;
            prof.editEmail(profileUser.getEmail(), email);
            System.out.println("Email изменён");
          } else {
            System.out.println("Вы не вошли в аккаунт");
          }
          break;
        case "7":
          if (inAccount) {
            System.out.print("Старый пароль: ");
            String oldPassword = input.next();
            System.out.print("Новый пароль: ");
            password = input.next();
            assert prof != null;
            prof.editPassword(oldPassword, password, password);
            System.out.println("Пароль изменён");
          } else {
            System.out.println("Вы не вошли в аккаунт");
          }
          break;
        case "5":
          if (inAccount) {
            System.out.print("Новый логин: ");
            login = input.next();
            assert prof != null;
            prof.editLogin(login);
            System.out.println("Логин изменён");
          } else {
            System.out.println("Вы не вошли в аккаунт");
          }
          break;
      }
    }
  }
  public static void main(String[] args) {
    fullRegCheck();
  }
}