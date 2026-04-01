package com.mipt.wallet.event;

import com.mipt.wallet.model.Operation;
import com.mipt.wallet.model.Wallet;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEvent {
 private String eventType;
 private UUID walletOwnerId;
 private UUID operationId;
 private UUID clientId;
 private UUID performerId;
 private String operationType;
 private Long amount;
 private String title;
 private String details;
 private Instant timestamp;

 public static WalletEvent walletCreated(Wallet wallet) {
  return WalletEvent.builder()
    .eventType("WALLET_CREATED")
    .walletOwnerId(wallet.getOwnerId())
    .details("Wallet created")
    .timestamp(Instant.now())
    .build();
 }

 public static WalletEvent operationCreated(Operation operation) {
  return WalletEvent.builder()
    .eventType("WALLET_OPERATION_CREATED")
    .walletOwnerId(operation.getClientId())
    .operationId(operation.getId())
    .clientId(operation.getClientId())
    .performerId(operation.getPerformerId())
    .operationType(operation.getType() != null ? operation.getType().name() : null)
    .amount(operation.getAmount())
    .title(operation.getTitle())
    .details("Wallet operation created")
    .timestamp(Instant.now())
    .build();
 }
}
