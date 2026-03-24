package com.mipt.checks;

import com.mipt.model.chat.Chat;
import com.mipt.model.chat.Message;
import com.mipt.service.MessageService;
import com.mipt.model.user.User;
import com.mipt.repository.user.UserRepository;
import com.mipt.config.DatabaseConfig;

import java.util.Scanner;
import java.util.UUID;

public class ChatCheck {

  public static void main(String[] args) {
    try {
      System.out.println("=== Сценарий: Вход в пользователя и отправка сообщений ===");

      UserRepository userRepository = new UserRepository();
      MessageService messageService = new MessageService();
      Scanner input = new Scanner(System.in);

      System.out.print("Введите email пользователя: ");
      User user = userRepository.findByEmail(input.next()).get();

      while (true) {
        System.out.println("Выберите команду:");
        System.out.println("1) Получить список активных чатов.");
        System.out.println("2) Начать чат с пользователем.");
        System.out.println("3) Удалить чат с пользователем.");
        System.out.println("4) Получить список сообщений с пользователем.");
        System.out.println("5) Отправить сообщение пользователю.");
        System.out.println("6) Отредактировать сообщение.");
        System.out.println("7) Удалить сообщение.");
        System.out.println("8) Прочитать сообщение.");
        System.out.println("9) Восстановить тестовые данные в таблицах.");
        System.out.println("10) Выход.");

        String call = input.next();

        switch (call) {
          case "1": {
            System.out.println("Активные чаты пользователя " + user.getLogin() + ": ");
            for (Chat chat : messageService.findAllChatsByUserId(user.getUserID())) {
              System.out.println("ID чата: " + chat.getId());
              System.out.println("Владелец чата: " + userRepository.findById(chat.getOwnerId()).get().getLogin());
              System.out.println("Участник чата: " + userRepository.findById(chat.getMemberId()).get().getLogin());
              System.out.println("Последняя активность в чате: " + chat.getLastUpdate());
              System.out.println();
            }

            break;
          }

          case "2": {
            System.out.print("Введите email пользователя: ");
            User receiver = userRepository.findByEmail(input.next()).get();

            messageService.addChat(Chat.builder().ownerId(user.getUserID()).memberId(receiver.getUserID()).build());
            System.out.println("Чат создан успешно.");

            break;
          }

          case "3": {
            System.out.print("Введите email пользователя: ");
            User receiver = userRepository.findByEmail(input.next()).get();

            Chat chat = messageService.findChatByUsersId(user.getUserID(), receiver.getUserID());

            messageService.deleteChat(chat.getId());
            System.out.println("Чат удалён успешно.");

            break;
          }

          case "4": {
            System.out.print("Введите email собеседника: ");
            User receiver = userRepository.findByEmail(input.next()).get();

            Chat chat = messageService.findChatByUsersId(user.getUserID(), receiver.getUserID());

            System.out.println("Список всех сообщений:");
            for (Message message : messageService.findAllMessagesByChatId(chat.getId())) {
              System.out.println("ID сообщения: " + message.getId());
              System.out.println("Отправитель: " + userRepository.findById(message.getSenderId()).get().getLogin());
              System.out.println("'" + message.getText() + "'");
              System.out.println("Отправлено в " + message.getSendingTime());
              System.out.println("Изменено в " + message.getEditingTime());
              System.out.println(message.isRead() ? "Прочитано" : "Не прочитано");
              System.out.println();
            }

            break;
          }

          case "5": {
            System.out.print("Введите email получателя: ");
            User receiver = userRepository.findByEmail(input.next()).get();

            Chat chat = messageService.findChatByUsersId(user.getUserID(), receiver.getUserID());

            input.nextLine();

            System.out.print("Введите сообщение: ");
            messageService.sendMessage(chat.getId(), user.getUserID(), input.nextLine());
            System.out.println("Сообщение отправлено успешно.");

            break;
          }

          case "6": {
            System.out.print("Введите ID сообщения: ");
            String id = input.next();

            input.nextLine();

            System.out.print("Введите новый текст сообщения: ");
            messageService.editMessage(UUID.fromString(id), input.nextLine());
            System.out.println("Сообщение отредактировано успешно.");

            break;
          }

          case "7": {
            System.out.print("Введите ID сообщения: ");
            messageService.deleteMessage(UUID.fromString(input.next()));
            System.out.println("Сообщение удалено успешно.");

            break;
          }

          case "8": {
            System.out.print("Введите ID сообщения: ");
            try {
              messageService.markAsRead(user.getUserID(), UUID.fromString(input.next()));
            } catch (Exception e) {
              System.out.println(e.getMessage());

              break;
            }

            System.out.println("Сообщение прочитано.");

            break;
          }

          case "9": {
            DatabaseConfig.recreateAllTables();
            System.out.println("Таблицы восстановлены успешно.");

            break;
          }

          case "10": {
            return;
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Ошибка во время тестирования: " + e.getMessage());
      System.out.println("=== Сценарий завершён с ошибкой ===");
    }
  }
}
