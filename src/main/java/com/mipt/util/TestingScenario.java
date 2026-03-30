package com.mipt.util;

import java.util.UUID;

import com.mipt.chat.model.Chat;
import com.mipt.chat.repository.ChatRepository;
import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.repository.WalletRepository;

public class TestingScenario {
  public static void run() {
    try {
      WalletRepository walletRepository = new WalletRepository();
      Wallet wallet =
          Wallet.builder()
              .ownerId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
              .availableTokens(100)
              .reservedTokens(50)
              .build();
      walletRepository.create(wallet);
      ChatRepository chatRepository = new ChatRepository();
      Chat chat =
          Chat.builder()
              .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
              .ownerId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
              .memberId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
              .build();
      chatRepository.create(chat);
    } catch (Exception e) {
      System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
    }
  }
}
