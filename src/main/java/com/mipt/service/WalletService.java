package com.mipt.service;

import com.mipt.model.wallet.Operation;
import com.mipt.model.wallet.Wallet;

import java.util.List;
import java.util.UUID;

public interface WalletService {

  void addWallet(Wallet wallet);

  List<Operation> findAllOperationsByWalletId(UUID walletId);

  Operation payTokens(UUID clientId, UUID performerId, Long amount, String title);

  Operation refundTokens(UUID clientId, UUID performerId, Long amount, String title);

  Operation reserveTokens(UUID clientId, UUID performerId, Long amount, String title);

  Operation cancelTokens(UUID clientId, UUID performerId, Long amount, String title);
}
