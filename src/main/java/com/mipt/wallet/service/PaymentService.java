package com.mipt.wallet.service;

import com.mipt.wallet.model.Operation;
import com.mipt.wallet.model.OperationType;
import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.repository.OperationRepository;
import com.mipt.wallet.repository.WalletRepository;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService implements WalletService {

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private OperationRepository operationRepository;

  @Override
  public void addWallet(Wallet wallet) {
    if (walletRepository.existsById(wallet.getOwnerId())) {
      throw new RuntimeException("Wallet with this ID already exists.");
    }

    walletRepository.save(wallet);
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
    Wallet clientWallet = walletRepository.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client wallet not found."));

    Wallet performerWallet = walletRepository.findById(performerId)
        .orElseThrow(() -> new RuntimeException("Performer wallet not found."));

    if (clientWallet.getReservedTokens() < amount) {
      throw new RuntimeException("Not enough reserved tokens for operation.");
    }

    Operation payOperation = Operation.builder()
        .id(UUID.randomUUID())
        .clientId(clientId)
        .performerId(performerId)
        .amount(amount)
        .type(OperationType.PAY)
        .title(title)
        .time(Instant.now())
        .build();

    clientWallet.setReservedTokens(clientWallet.getReservedTokens() - amount);

    performerWallet.setAvailableTokens(performerWallet.getAvailableTokens() + amount);

    walletRepository.save(clientWallet);
    walletRepository.save(performerWallet);

    return operationRepository.save(payOperation);
  }

  @Override
  public Operation refundTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client wallet not found."));

    Wallet performerWallet = walletRepository.findById(performerId)
        .orElseThrow(() -> new RuntimeException("Performer wallet not found."));

    if (performerWallet.getAvailableTokens() < amount) {
      throw new RuntimeException("Not enough available tokens for operation.");
    }

    Operation refundOperation = Operation.builder()
        .id(UUID.randomUUID())
        .clientId(clientId)
        .performerId(performerId)
        .amount(amount)
        .type(OperationType.REFUND)
        .title(title)
        .time(Instant.now())
        .build();

    clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() + amount);

    performerWallet.setAvailableTokens(performerWallet.getAvailableTokens() - amount);

    walletRepository.save(clientWallet);
    walletRepository.save(performerWallet);

    return operationRepository.save(refundOperation);
  }

  @Override
  public Operation reserveTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client wallet not found."));

    if (clientWallet.getAvailableTokens() < amount) {
      throw new RuntimeException("Not enough available tokens for operation.");
    }

    Operation reserveOperation = Operation.builder()
        .id(UUID.randomUUID())
        .clientId(clientId)
        .performerId(performerId)
        .amount(amount)
        .type(OperationType.RESERVE)
        .title(title)
        .time(Instant.now())
        .build();

    clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() - amount);

    clientWallet.setReservedTokens(clientWallet.getReservedTokens() + amount);

    walletRepository.save(clientWallet);

    return operationRepository.save(reserveOperation);
  }

  @Override
  public Operation cancelTokens(UUID clientId, UUID performerId, Long amount, String title) {
    Wallet clientWallet = walletRepository.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client wallet not found."));

    if (clientWallet.getReservedTokens() < amount) {
      throw new RuntimeException("Not enough reserved tokens for operation.");
    }

    Operation cancelOperation = Operation.builder()
        .id(UUID.randomUUID())
        .clientId(clientId)
        .performerId(performerId)
        .amount(amount)
        .type(OperationType.CANCEL)
        .title(title)
        .time(Instant.now())
        .build();

    clientWallet.setReservedTokens(clientWallet.getReservedTokens() - amount);

    clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() + amount);

    walletRepository.save(clientWallet);

    return operationRepository.save(cancelOperation);
  }
}
