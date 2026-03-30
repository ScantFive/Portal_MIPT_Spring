package com.mipt.wallet.service;

import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletDataService {

  private final WalletJpaRepository repository;

  public Wallet create(Wallet wallet) {
    return repository.save(wallet);
  }

  public Wallet createWithUserId(UUID ownerId) {
    Wallet wallet = Wallet.builder()
        .ownerId(ownerId)
        .availableTokens(0L)
        .reservedTokens(0L)
        .build();
    return repository.save(wallet);
  }

  public boolean existsById(UUID id) {
    return repository.existsById(id);
  }

  public Wallet findById(UUID ownerId) {
    return repository.findById(ownerId)
        .orElseThrow(() -> new IllegalArgumentException("Wallet not found for owner: " + ownerId));
  }

  public List<Wallet> findAll() {
    return repository.findAll();
  }

  public void update(Wallet newWallet) {
    repository.save(newWallet);
  }

  public void deleteById(UUID ownerId) {
    repository.deleteById(ownerId);
  }
}
