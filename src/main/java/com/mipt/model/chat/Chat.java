package com.mipt.model.chat;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
public class Chat {
  @Builder.Default
  final UUID id = UUID.randomUUID();
  UUID ownerId;
  UUID memberId;
  @Builder.Default
  Instant lastUpdate = Instant.now();
}
