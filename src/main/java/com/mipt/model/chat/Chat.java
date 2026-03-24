package com.mipt.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
  @Default final UUID id = UUID.randomUUID();
  UUID ownerId;
  UUID memberId;
  @Default Instant lastUpdate = Instant.now();
}
