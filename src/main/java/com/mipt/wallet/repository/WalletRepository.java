package com.mipt.wallet.repository;

import com.mipt.util.SpringContext;
import com.mipt.wallet.model.Wallet;
import java.util.List;
import java.util.UUID;

public class WalletRepository {

  private WalletJpaRepository repository() {
    return SpringContext.getBean(WalletJpaRepository.class);
  }

  public Wallet create(Wallet wallet) {
    return repository().save(wallet);
  }

  public Wallet createWithUserId(UUID ownerId) {
    Wallet wallet = Wallet.builder().ownerId(ownerId).build();
    return create(wallet);
  }

  public boolean existsById(UUID id) {
    return repository().existsById(id);
  }

  public Wallet findById(UUID ownerId) {
    return repository().findById(ownerId)
        .orElseThrow(() -> new RuntimeException("No wallet with this owner ID."));
  }

  public List<Wallet> findAll() {
    return repository().findAll();
  }

  public void update(Wallet newWallet) {
    repository().save(newWallet);
  }

  public void deleteById(UUID ownerId) {
    repository().deleteById(ownerId);
  }
}
