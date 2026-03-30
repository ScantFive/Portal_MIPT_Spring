package com.mipt.chat.repository;

import com.mipt.chat.model.Chat;
import com.mipt.util.SpringContext;
import java.util.List;
import java.util.UUID;

public class ChatRepository {

  private ChatJpaRepository repository() {
    return SpringContext.getBean(ChatJpaRepository.class);
  }

  public Chat create(Chat chat) {
    return repository().save(chat);
  }

  public boolean existsById(UUID id) {
    return repository().existsById(id);
  }

  public Chat findById(UUID id) {
    return repository().findById(id)
        .orElseThrow(() -> new RuntimeException("No chat with this ID."));
  }

  public Chat findByOwnerAndMemberId(UUID ownerId, UUID memberId) {
    return repository().findByOwnerIdAndMemberId(ownerId, memberId)
        .orElseThrow(() -> new RuntimeException("No chat with this ID."));
  }

  public List<Chat> findAll() {
    return repository().findAll();
  }

  public void update(Chat newChat) {
    repository().save(newChat);
  }

  public void deleteById(UUID id) {
    repository().deleteById(id);
  }
}
