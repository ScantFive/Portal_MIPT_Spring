package com.mipt.checks;



import com.mipt.model.user.User;
import com.mipt.model.wallet.Operation;
import com.mipt.model.wallet.Wallet;
import com.mipt.repository.user.UserRepository;
import com.mipt.repository.wallet.WalletRepository;
import com.mipt.service.PaymentService;

import java.util.Scanner;

public class WalletCheck {

  public static void main(String[] args) {
    try {
      System.out.println("=== Сценарий: Вход в пользователя и работа с кошельком ===");

      UserRepository userRepository = new UserRepository();
      WalletRepository walletRepository = new WalletRepository();
      PaymentService paymentService = new PaymentService();
      Scanner input = new Scanner(System.in);

      System.out.print("Введите email пользователя: ");
      User user = userRepository.findByEmail(input.next()).get();

      while (true) {
        System.out.println("Выберите команду:");
        System.out.println("0) Выход.");
        System.out.println("1) Получить баланс кошелька.");
        System.out.println("2) Получить историю операций.");
        System.out.println("3) Отправить токены пользователю.");
        System.out.println("4) Вернуть токены от пользователя.");
        System.out.println("5) Зарезервировать токены.");
        System.out.println("6) Забрать токены из резера.");
        String call = input.next();
        switch (call) {
          case "0": {
            return;
          }

          case "1": {
            Wallet wallet = walletRepository.findById(user.getUserID());
            System.out.println(
                "Баланс: " + wallet.getAvailableTokens() + " (" + wallet.getReservedTokens() + " зарезервированно)");
            break;
          }

          case "2": {
            for (Operation operation : paymentService.findAllOperationsByWalletId(user.getUserID())) {
              System.out.println("ID операции: " + operation.getId());
              System.out.println("'" + operation.getTitle() + "'");
              System.out.println("Клиент: " + userRepository.findById(operation.getClientId()).get().getLogin());
              System.out
                  .println("Исполнитель: " + userRepository.findById(operation.getPerformerId()).get().getLogin());
              System.out.println("Сумма: " + operation.getAmount());
              System.out.println("Время операции: " + operation.getTime());
              System.out.println("Тип операции: " + operation.getType());
            }
            break;
          }

          case "3": {
            try {
              System.out.print("Введите email пользователя: ");
              User receiver = userRepository.findByEmail(input.next()).get();
              System.out.print("Введите сумму: ");
              Long amount = input.nextLong();
              System.out.print("Введите сообщение: ");
              String title = input.next();
              paymentService.payTokens(user.getUserID(), receiver.getUserID(), amount, title);
              System.out.println("Токены отправлены успешно.");
            } catch (RuntimeException e) {
              System.err.println(e.getMessage());
            }
            break;
          }

          case "4": {
            try {
              System.out.print("Введите email пользователя: ");
              User receiver = userRepository.findByEmail(input.next()).get();
              System.out.print("Введите сумму: ");
              Long amount = input.nextLong();
              System.out.print("Введите сообщение: ");
              String title = input.next();
              paymentService.refundTokens(user.getUserID(), receiver.getUserID(), amount, title);
              System.out.println("Токены возвращены успешно.");
            } catch (RuntimeException e) {
              System.err.println(e.getMessage());
            }
            break;
          }

          case "5": {
            try {
              System.out.print("Введите email пользователя: ");
              User receiver = userRepository.findByEmail(input.next()).get();
              System.out.print("Введите сумму: ");
              Long amount = input.nextLong();
              System.out.print("Введите сообщение: ");
              String title = input.next();
              paymentService.reserveTokens(user.getUserID(), receiver.getUserID(), amount, title);
              System.out.println("Токены зарезервированы успешно.");
            } catch (RuntimeException e) {
              System.err.println(e.getMessage());
            }
            break;
          }

          case "6": {
            try {
              System.out.print("Введите email пользователя: ");
              User receiver = userRepository.findByEmail(input.next()).get();
              System.out.print("Введите сумму: ");
              Long amount = input.nextLong();
              System.out.print("Введите сообщение: ");
              String title = input.next();
              paymentService.cancelTokens(user.getUserID(), receiver.getUserID(), amount, title);
              System.out.println("Токены возвращены из резерва успешно.");
            } catch (RuntimeException e) {
              System.err.println(e.getMessage());
            }
            break;
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Ошибка во время тестирования: " + e.getMessage());
      System.out.println("=== Сценарий завершён с ошибкой ===");
    }
  }
}
