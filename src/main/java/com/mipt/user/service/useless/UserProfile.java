package com.mipt.user.service.useless;

import com.mipt.user.model.User;
import com.mipt.user.repository.useless.UserRepository;
import com.mipt.util.PasswordHasher;
import com.mipt.wallet.model.Operation;
import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.repository.OperationRepository;
import com.mipt.wallet.repository.WalletRepository;
import com.mipt.wallet.service.PaymentService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
