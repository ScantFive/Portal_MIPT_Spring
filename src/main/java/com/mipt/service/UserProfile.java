package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.repository.user.UserRepository;
import com.mipt.model.wallet.Operation;
import com.mipt.model.wallet.Wallet;
import com.mipt.repository.wallet.OperationRepository;
import com.mipt.repository.wallet.WalletRepository;
import com.mipt.service.util.PasswordHasher;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfile implements Profile {

  UserRepository userRepository = new UserRepository();
  WalletRepository walletRepository = new WalletRepository();
  OperationRepository operationRepository = new OperationRepository();
  PaymentService paymentService = new PaymentService();
  public User user;
  public Wallet wallet;

  public UserProfile(UUID userID) {
    Optional<User> userOptional = userRepository.findById(userID);
    this.user = userOptional.get();
    this.wallet = walletRepository.findById(userID);
  }

  @Override
  public User getInfo() {
    return user;
  }

  @Override
  public void editLogin(String newLogin) {
    user.setLogin(newLogin);
    userRepository.update(user);
  }

  @Override
  public void editEmail(String email, String newEmail) {

    user.setEmail(newEmail);
    userRepository.update(user);
  }

  @Override
  public void editPassword(String password, String newPassword, String confirmPassword) {
    user.setHashedPassword(PasswordHasher.hash(password));
    userRepository.update(user);
  }

  @Override
  public long getTokensAmount(UUID userID) {
    return wallet.getAvailableTokens();
  }

  @Override
  public List<Operation> dealHistory(UUID userID) {
    return paymentService.findAllOperationsByWalletId(userID);
  }

  @Override
  public void logOut() {}
}
