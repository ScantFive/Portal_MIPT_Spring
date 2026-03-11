package com.mipt.chat.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
@Builder
@Data
public class Message {
  @Id
  @Builder.Default
  final UUID id = UUID.randomUUID();

  @Column(name = "chat")
  final UUID chatId;

  @Column(name = "sender")
  final UUID senderId;

  @Column(name = "sending_time")
  @Builder.Default
  final Instant sendingTime = Instant.now();

  @Column(name = "text")
  String text;

  @Column(name = "editing_time")
  @Builder.Default
  Instant editingTime = Instant.EPOCH;

  @Column(name = "is_read")
  @Builder.Default
  boolean isRead = false;
}
