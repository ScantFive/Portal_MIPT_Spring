package com.mipt.wallet.service;

import com.mipt.util.SpringContext;
import com.mipt.wallet.event.WalletEvent;
import com.mipt.wallet.model.Operation;
import com.mipt.wallet.model.OperationType;
import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.repository.OperationRepository;
import com.mipt.wallet.repository.WalletRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentService implements WalletService {
  WalletRepository walletRepository = new WalletRepository();
  OperationRepository operationRepository = new OperationRepository();

  @Override
  public void addWallet(Wallet wallet) {
    if (walletRepository.existsById(wallet.getOwnerId())) {
      throw new RuntimeException("Wallet with this ID already exists.");
    }
    walletRepository.create(wallet);
    publishEvent(WalletEvent.walletCreated(wallet));
  }

  @Override
  public List<Operation> findAllOperationsByWalletId(UUID walletId) {
    List<Operation> wallet_operations = new ArrayList<>();
    for (Operation operation : operationRepository.findAll()) {
      if (operation.getClientId().equals(walletId) || operation.getPerformerId().equals(walletId)) {
        wallet_operations.add(operation);
      }
    }
    return wallet_operations;
  }

  @Override
  public Operation payTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId);
    Wallet performerWallet = walletRepository.findById(performerId);
    if (clientWallet.getReservedTokens() < amount) {
      throw new RuntimeException("Not enough reserved tokens for operation.");
    }
    Operation payOperation = Operation.builder().clientId(clientId).performerId(performerId).amount(amount)
        .type(OperationType.PAY).title(title).build();
    clientWallet.setReservedTokens(clientWallet.getReservedTokens() - amount);
    performerWallet.setAvailableTokens(performerWallet.getAvailableTokens() + amount);
    walletRepository.update(clientWallet);
    walletRepository.update(performerWallet);
    Operation created = operationRepository.create(payOperation);
    publishEvent(WalletEvent.operationCreated(created));
    return created;
  }

  @Override
  public Operation refundTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId);
    Wallet performerWallet = walletRepository.findById(performerId);
    if (performerWallet.getAvailableTokens() < amount) {
      throw new RuntimeException("Not enough available tokens for operation.");
    }
    Operation refundOperation = Operation.builder().clientId(clientId).performerId(performerId).amount(amount)
        .type(OperationType.REFUND).title(title).build();
    clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() + amount);
    performerWallet.setAvailableTokens(performerWallet.getAvailableTokens() - amount);
    walletRepository.update(clientWallet);
    walletRepository.update(performerWallet);
    Operation created = operationRepository.create(refundOperation);
    publishEvent(WalletEvent.operationCreated(created));
    return created;
  }

  @Override
  public Operation reserveTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId);
    if (clientWallet.getAvailableTokens() < amount) {
      throw new RuntimeException("Not enough available tokens for operation.");
    }
    Operation reserveOperation = Operation.builder().clientId(clientId).performerId(performerId).amount(amount)
        .type(OperationType.RESERVE).title(title).build();
    clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() - amount);
    clientWallet.setReservedTokens(clientWallet.getReservedTokens() + amount);
    walletRepository.update(clientWallet);
    Operation created = operationRepository.create(reserveOperation);
    publishEvent(WalletEvent.operationCreated(created));
    return created;
  }

  @Override
  public Operation cancelTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId);
    if (clientWallet.getReservedTokens() < amount) {
      throw new RuntimeException("Not enough reserved tokens for operation.");
    }
    Operation cancelOperation = Operation.builder().clientId(clientId).performerId(performerId).amount(amount)
        .type(OperationType.CANCEL).title(title).build();
    clientWallet.setReservedTokens(clientWallet.getReservedTokens() - amount);
    clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() + amount);
    walletRepository.update(clientWallet);
    Operation created = operationRepository.create(cancelOperation);
    publishEvent(WalletEvent.operationCreated(created));
    return created;
  }

  private void publishEvent(WalletEvent event) {
    try {
      SpringContext.getBean(WalletKafkaEventPublisher.class).publish(event);
    } catch (Exception ex) {
      log.error("Unable to publish WALLET event {}", event != null ? event.getEventType() : "unknown", ex);
    }
  }
}
