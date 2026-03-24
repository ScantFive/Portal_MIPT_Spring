package com.mipt.model.wallet;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class Wallet {
  final UUID ownerId;
  @Builder.Default long availableTokens = 0L;
  @Builder.Default long reservedTokens = 0L;
}
