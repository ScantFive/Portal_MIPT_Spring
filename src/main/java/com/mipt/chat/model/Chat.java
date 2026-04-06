package com.mipt.chat.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chats")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
  @Id
  @Column(name = "id")
  @Builder.Default
  private UUID id = UUID.randomUUID();

  @Column(name = "owner", nullable = false)
  private UUID ownerId;

  @Column(name = "member", nullable = false)
  private UUID memberId;

  @Column(name = "last_update", nullable = false)
  @Builder.Default
  private Instant lastUpdate = Instant.now();
}
