package com.mipt.service;

import com.mipt.model.wallet.Operation;
import com.mipt.model.wallet.OperationType;
import com.mipt.model.wallet.Wallet;
import com.mipt.repository.wallet.OperationRepository;
import com.mipt.repository.wallet.WalletRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Data
@RequiredArgsConstructor
@Transactional
public class PaymentService implements WalletService {
  WalletRepository walletRepository = new WalletRepository();
  OperationRepository operationRepository = new OperationRepository();

  @Override
  public void addWallet(Wallet wallet) {
    if (walletRepository.existsById(wallet.getOwnerId())) {
      throw new RuntimeException("Wallet with this ID already exists.");
    }
    walletRepository.create(wallet);
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
    return operationRepository.create(payOperation);
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
    return operationRepository.create(refundOperation);
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
    return operationRepository.create(reserveOperation);
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
    return operationRepository.create(cancelOperation);
  }
}
