package com.mipt.model.chat;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@Entity
public class Message {
  @Builder.Default
  final UUID id = UUID.randomUUID();
  final UUID chatId;
  final UUID senderId;
  @Builder.Default
  final Instant sendingTime = Instant.now();
  String text;
  @Builder.Default
  Instant editingTime = Instant.EPOCH;
  @Builder.Default
  boolean isRead = false;
}
