package com.mipt.service;

import com.mipt.model.user.User;
import com.mipt.model.wallet.Operation;

import java.util.List;
import java.util.UUID;

public interface Profile {

  User getInfo();

  void editLogin(String newLogin);

  void editEmail(String email, String newEmail);

  void editPassword(String password, String newPassword, String confirmPassword);

  long getTokensAmount(UUID userID);

  List<Operation> dealHistory(UUID userID); // когда появится класс сделка, заменю на неё

  void logOut();
}
