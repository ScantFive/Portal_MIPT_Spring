package com.mipt.chat.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

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
