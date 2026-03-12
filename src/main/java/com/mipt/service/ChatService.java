package com.mipt.service;

import com.mipt.model.chat.Chat;
import com.mipt.model.chat.Message;

import java.util.List;
import java.util.UUID;

public interface ChatService {

  void addChat(Chat chat);

  void deleteChat(UUID chatId);

  Chat findChatByUsersId(UUID firstUserId, UUID secondUserId);

  List<Chat> findAllChatsByUserId(UUID userId);

  List<Message> findAllMessagesByChatId(UUID chatId);

  Message sendMessage(UUID chatId, UUID senderId, String text);

  Message editMessage(UUID messageId, String text);

  void deleteMessage(UUID messageId);

  void markAsRead(UUID userId, UUID messageId);
}
