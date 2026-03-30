package com.mipt.chat.repository;

import com.mipt.chat.model.Message;
import com.mipt.util.SpringContext;
import java.util.List;
import java.util.UUID;

public class MessageRepository {

  private MessageJpaRepository repository() {
    return SpringContext.getBean(MessageJpaRepository.class);
  }

  public Message create(Message message) {
    return repository().save(message);
  }

  public boolean existsById(UUID id) {
    return repository().existsById(id);
  }

  public Message findById(UUID id) {
    return repository().findById(id)
        .orElseThrow(() -> new RuntimeException("No message with this ID."));
  }

  public List<Message> findByChatId(UUID chatId) {
    return repository().findByChatId(chatId);
  }

  public List<Message> findAll() {
    return repository().findAll();
  }

  public void update(Message newMessage) {
    repository().save(newMessage);
  }

  public void deleteById(UUID id) {
    repository().deleteById(id);
  }

  public void deleteByChatId(UUID chatId) {
    repository().deleteByChatId(chatId);
  }
}
