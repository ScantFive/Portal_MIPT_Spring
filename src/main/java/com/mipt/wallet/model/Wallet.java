package com.mipt.wallet.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
  @Id
  @Column(name = "owner")
  UUID ownerId;

  @Builder.Default
  @Column(name = "available_tokens")
  long availableTokens = 0L;

  @Builder.Default
  @Column(name = "reserved_tokens")
  long reservedTokens = 0L;
}
