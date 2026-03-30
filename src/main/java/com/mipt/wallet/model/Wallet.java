package com.mipt.wallet.model;

import jakarta.persistence.*;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

  @Id
  @Column(name = "owner_id")
  private UUID ownerId;

  @Column(name = "available_tokens", nullable = false)
  @Builder.Default
  private long availableTokens = 0L;

  @Column(name = "reserved_tokens", nullable = false)
  @Builder.Default
  private long reservedTokens = 0L;
}
