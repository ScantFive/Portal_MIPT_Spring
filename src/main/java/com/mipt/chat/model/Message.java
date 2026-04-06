package com.mipt.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
  @Id
  @Column(name = "id")
  @Builder.Default
  private UUID id = UUID.randomUUID();

  @Column(name = "chat", nullable = false)
  private UUID chatId;

  @Column(name = "sender")
  private UUID senderId;

  @Column(name = "sending_time", nullable = false)
  @Builder.Default
  private Instant sendingTime = Instant.now();

  @Column(name = "text")
  private String text;

  @Column(name = "editing_time", nullable = false)
  @Builder.Default
  private Instant editingTime = Instant.EPOCH;

  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private boolean read = false;
}
