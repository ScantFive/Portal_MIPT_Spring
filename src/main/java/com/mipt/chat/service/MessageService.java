package com.mipt.chat.service;

import com.mipt.chat.event.ChatEvent;
import com.mipt.chat.model.Chat;
import com.mipt.chat.model.Message;
import com.mipt.chat.repository.ChatRepository;
import com.mipt.chat.repository.MessageRepository;
import com.mipt.util.SpringContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService implements ChatService {
  ChatRepository chatRepository = new ChatRepository();
  MessageRepository messageRepository = new MessageRepository();

  @Override
  public void addChat(Chat chat) {
    if (chatRepository.existsById(chat.getId())) {
      throw new RuntimeException("Chat with this ID already exists.");
    }
    chatRepository.create(chat);
    publishEvent(ChatEvent.chatCreated(chat));
  }

  @Override
  public void deleteChat(UUID chatId) {
    if (!chatRepository.existsById(chatId)) {
      throw new RuntimeException("No chats with this ID.");
    }
    Chat existing = chatRepository.findById(chatId);
    chatRepository.deleteById(chatId);
    messageRepository.deleteByChatId(chatId);
    publishEvent(ChatEvent.chatDeleted(existing));
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
    Message created = messageRepository.create(message);
    publishEvent(ChatEvent.messageSent(created, resolveReceiver(chat, senderId)));
    return created;
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
    publishEvent(ChatEvent.messageEdited(message, resolveReceiverByMessage(message)));
    return message;
  }

  @Override
  public void deleteMessage(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      throw new RuntimeException("No messages with this ID.");
    }
    Message message = messageRepository.findById(messageId);
    messageRepository.deleteById(messageId);
    publishEvent(ChatEvent.messageDeleted(message, resolveReceiverByMessage(message)));
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
    publishEvent(ChatEvent.messageRead(message, userId));
  }

  private UUID resolveReceiver(Chat chat, UUID senderId) {
    if (chat == null) {
      return null;
    }
    if (senderId != null && senderId.equals(chat.getOwnerId())) {
      return chat.getMemberId();
    }
    return chat.getOwnerId();
  }

  private UUID resolveReceiverByMessage(Message message) {
    Chat chat = chatRepository.findById(message.getChatId());
    return resolveReceiver(chat, message.getSenderId());
  }

  private void publishEvent(ChatEvent event) {
    try {
      SpringContext.getBean(ChatKafkaEventPublisher.class).publish(event);
    } catch (Exception ex) {
      log.error("Unable to publish CHAT event {}", event != null ? event.getEventType() : "unknown", ex);
    }
  }
}
