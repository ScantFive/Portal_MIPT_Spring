package com.mipt.service;

import com.mipt.model.chat.Chat;
import com.mipt.model.chat.Message;
import com.mipt.repository.chat.ChatRepository;
import com.mipt.repository.chat.MessageRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
@Transactional
public class MessageService implements ChatService {
  ChatRepository chatRepository;
  MessageRepository messageRepository;

  @Override
  public void addChat(Chat chat) {
    if (chatRepository.existsById(chat.getId())) {
      throw new RuntimeException("Chat with this ID already exists.");
    }
    chatRepository.create(chat);
  }

  @Override
  public void deleteChat(UUID chatId) {
    if (!chatRepository.existsById(chatId)) {
      throw new RuntimeException("No chats with this ID.");
    }
    chatRepository.deleteById(chatId);
    messageRepository.deleteByChatId(chatId);
  }

  @Override
  public Chat findChatByUsersId(UUID firstUserId, UUID secondUserId) {
    Chat chat;

    try {
      chat = chatRepository.findByOwnerAndMemberId(firstUserId, secondUserId);
    } catch (Exception e) {
      chat = chatRepository.findByOwnerAndMemberId(secondUserId, firstUserId);
    }

    return chat;
  }

  @Override
  public List<Chat> findAllChatsByUserId(UUID userId) {
    List<Chat> user_chats = new ArrayList<>();
    for (Chat chat : chatRepository.findAll()) {
      if (chat.getOwnerId().equals(userId) || chat.getMemberId().equals(userId)) {
        user_chats.add(chat);
      }
    }
    user_chats.sort(new ChatComparator());
    return user_chats;
  }

  @Override
  public List<Message> findAllMessagesByChatId(UUID chatId) {
    List<Message> messages = messageRepository.findByChatId(chatId);
    messages.sort(new MessageComparator());
    return messages;
  }

  @Override
  public Message sendMessage(UUID chatId, UUID senderId, String text) {
    Message message = Message.builder().chatId(chatId).senderId(senderId).text(text).build();
    Chat chat = chatRepository.findById(chatId);
    chat.setLastUpdate(message.getSendingTime());
    chatRepository.update(chat);
    return messageRepository.create(message);
  }

  @Override
  public Message editMessage(UUID messageId, String text) {
    if (!messageRepository.existsById(messageId)) {
      throw new RuntimeException("No messages with this ID.");
    }
    Message message = messageRepository.findById(messageId);
    message.setText(text);
    message.setEditingTime(Instant.now());
    messageRepository.update(message);
    return message;
  }

  @Override
  public void deleteMessage(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      throw new RuntimeException("No messages with this ID.");
    }
    messageRepository.deleteById(messageId);
  }

  @Override
  public void markAsRead(UUID userId, UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      throw new RuntimeException("No messages with this ID.");
    }

    Message message = messageRepository.findById(messageId);

    if (userId.equals(message.getSenderId())) {
      throw new RuntimeException("Owner cannot read his own message.");
    }

    message.setRead(true);
    messageRepository.update(message);
  }
}
