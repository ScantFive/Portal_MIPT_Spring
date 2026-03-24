package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.model.wallet.Operation;

import java.util.List;
import java.util.UUID;

public interface Profile {

  User getInfo(UUID userId);

  void editLogin(UUID userId, String newLogin);

  void editEmail(UUID userId, String newEmail);

  void editPassword(UUID userId, String oldPassword, String newPassword, String confirmPassword);

  long getTokensAmount(UUID userId);

  List<Operation> dealHistory(UUID userId);

  void logOut();
}
