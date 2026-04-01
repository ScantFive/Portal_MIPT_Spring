package com.mipt.chat.event;

import com.mipt.chat.model.Chat;
import com.mipt.chat.model.Message;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {
 private String eventType;
 private UUID chatId;
 private UUID messageId;
 private UUID senderId;
 private UUID receiverId;
 private String messageText;
 private String actionType;
 private Instant timestamp;

 public static ChatEvent chatCreated(Chat chat) {
  return ChatEvent.builder()
    .eventType("CHAT_CREATED")
    .chatId(chat.getId())
    .senderId(chat.getOwnerId())
    .receiverId(chat.getMemberId())
    .actionType("CREATE_CHAT")
    .timestamp(Instant.now())
    .build();
 }

 public static ChatEvent chatDeleted(Chat chat) {
  return ChatEvent.builder()
    .eventType("CHAT_DELETED")
    .chatId(chat.getId())
    .senderId(chat.getOwnerId())
    .receiverId(chat.getMemberId())
    .actionType("DELETE_CHAT")
    .timestamp(Instant.now())
    .build();
 }

 public static ChatEvent messageSent(Message message, UUID receiverId) {
  return ChatEvent.builder()
    .eventType("MESSAGE_RECEIVED")
    .chatId(message.getChatId())
    .messageId(message.getId())
    .senderId(message.getSenderId())
    .receiverId(receiverId)
    .messageText(message.getText())
    .actionType("SEND_MESSAGE")
    .timestamp(Instant.now())
    .build();
 }

 public static ChatEvent messageEdited(Message message, UUID receiverId) {
  return ChatEvent.builder()
    .eventType("MESSAGE_EDITED")
    .chatId(message.getChatId())
    .messageId(message.getId())
    .senderId(message.getSenderId())
    .receiverId(receiverId)
    .messageText(message.getText())
    .actionType("EDIT_MESSAGE")
    .timestamp(Instant.now())
    .build();
 }

 public static ChatEvent messageDeleted(Message message, UUID receiverId) {
  return ChatEvent.builder()
    .eventType("MESSAGE_DELETED")
    .chatId(message.getChatId())
    .messageId(message.getId())
    .senderId(message.getSenderId())
    .receiverId(receiverId)
    .messageText(message.getText())
    .actionType("DELETE_MESSAGE")
    .timestamp(Instant.now())
    .build();
 }

 public static ChatEvent messageRead(Message message, UUID readerId) {
  return ChatEvent.builder()
    .eventType("MESSAGE_READ")
    .chatId(message.getChatId())
    .messageId(message.getId())
    .senderId(message.getSenderId())
    .receiverId(readerId)
    .messageText(message.getText())
    .actionType("READ_MESSAGE")
    .timestamp(Instant.now())
    .build();
 }
}
