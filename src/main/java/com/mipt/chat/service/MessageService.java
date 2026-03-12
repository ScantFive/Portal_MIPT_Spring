package com.mipt.chat.service;

import com.mipt.chat.model.Chat;
import com.mipt.chat.model.Message;
import com.mipt.chat.repository.ChatRepository;
import com.mipt.chat.repository.MessageRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService implements ChatService {
  @Autowired
  private ChatRepository chatRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Override
  public void addChat(Chat chat) {
    if (chatRepository.existsById(chat.getId())) {
      throw new RuntimeException("Chat with this ID already exists.");
    }
    chatRepository.save(chat);
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
      chat = chatRepository.findByOwnerIdAndMemberId(firstUserId, secondUserId)
          .orElseThrow(() -> new RuntimeException("No chat with this ID."));
    } catch (Exception e) {
      chat = chatRepository.findByOwnerIdAndMemberId(secondUserId, firstUserId)
          .orElseThrow(() -> new RuntimeException("No chat with this ID."));
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
    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new RuntimeException("No chat with this ID."));
    chat.setLastUpdate(message.getSendingTime());
    chatRepository.save(chat);
    return messageRepository.save(message);
  }

  @Override
  public Message editMessage(UUID messageId, String text) {
    if (!messageRepository.existsById(messageId)) {
      throw new RuntimeException("No messages with this ID.");
    }
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new RuntimeException("No messages with this ID."));
    message.setText(text);
    message.setEditingTime(Instant.now());
    messageRepository.save(message);
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

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new RuntimeException("No messages with this ID."));

    if (userId.equals(message.getSenderId())) {
      throw new RuntimeException("Owner cannot read his own message.");
    }

    message.setRead(true);
    messageRepository.save(message);
  }
}
