package com.mipt.model.wallet;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
public class Operation {
  @Id
  @Builder.Default
  final UUID id = UUID.randomUUID();
  final UUID clientId;
  final UUID performerId;
  final Long amount;
  final OperationType type;
  @Builder.Default
  final Instant time = Instant.now();
  String title;
}
