package com.mipt.model.chat;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@Entity
public class Chat {
  @Builder.Default final UUID id = UUID.randomUUID();
  UUID ownerId;
  UUID memberId;
  @Builder.Default Instant lastUpdate = Instant.now();
}
