package com.mipt.chat.repository;

import com.mipt.chat.model.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, UUID> {
  List<Message> findByChatId(UUID chatId);

  void deleteByChatId(UUID chatId);
}
