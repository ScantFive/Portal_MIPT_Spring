package com.mipt.chat.repository;

import com.mipt.chat.model.Chat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatJpaRepository extends JpaRepository<Chat, UUID> {
  Optional<Chat> findByOwnerIdAndMemberId(UUID ownerId, UUID memberId);
}
