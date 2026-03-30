package com.mipt.chat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mipt.chat.model.Chat;
import com.mipt.chat.model.Message;
import com.mipt.chat.repository.ChatRepository;
import com.mipt.chat.repository.MessageRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatServiceImplTest {

  @Test
  void addChatShouldFailWhenExists() {
    MessageService service = new MessageService();
    service.chatRepository = mock(ChatRepository.class);
    Chat chat = Chat.builder().id(UUID.randomUUID()).ownerId(UUID.randomUUID()).memberId(UUID.randomUUID()).build();

    when(service.chatRepository.existsById(chat.getId())).thenReturn(true);

    assertThrows(RuntimeException.class, () -> service.addChat(chat));
  }

  @Test
  void findChatByUsersIdShouldTryReversedOrder() {
    MessageService service = new MessageService();
    service.chatRepository = mock(ChatRepository.class);
    UUID u1 = UUID.randomUUID();
    UUID u2 = UUID.randomUUID();
    Chat existing = Chat.builder().id(UUID.randomUUID()).ownerId(u2).memberId(u1).build();

    when(service.chatRepository.findByOwnerAndMemberId(u1, u2)).thenThrow(new RuntimeException("not found"));
    when(service.chatRepository.findByOwnerAndMemberId(u2, u1)).thenReturn(existing);

    Chat result = service.findChatByUsersId(u1, u2);

    assertEquals(existing.getId(), result.getId());
  }

  @Test
  void sendMessageShouldUpdateChatAndPersistMessage() {
    MessageService service = new MessageService();
    service.chatRepository = mock(ChatRepository.class);
    service.messageRepository = mock(MessageRepository.class);

    UUID sender = UUID.randomUUID();
    UUID receiver = UUID.randomUUID();
    UUID chatId = UUID.randomUUID();
    Chat chat = Chat.builder().id(chatId).ownerId(sender).memberId(receiver).lastUpdate(Instant.EPOCH).build();

    when(service.chatRepository.findById(chatId)).thenReturn(chat);
    when(service.messageRepository.create(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Message result = service.sendMessage(chatId, sender, "hello");

    assertNotNull(result);
    assertEquals("hello", result.getText());
    verify(service.chatRepository).update(any(Chat.class));
    verify(service.messageRepository).create(any(Message.class));
  }

  @Test
  void getChatMessagesShouldReturnHistory() {
    MessageService service = new MessageService();
    service.messageRepository = mock(MessageRepository.class);
    UUID chatId = UUID.randomUUID();
    Message m = Message.builder().chatId(chatId).text("x").sendingTime(Instant.now()).build();

    when(service.messageRepository.findByChatId(chatId)).thenReturn(new ArrayList<>(List.of(m)));

    List<Message> result = service.findAllMessagesByChatId(chatId);

    assertEquals(1, result.size());
    assertEquals("x", result.get(0).getText());
  }
}
