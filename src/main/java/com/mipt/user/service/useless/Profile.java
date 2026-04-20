package com.mipt.user.service.useless;

import com.mipt.user.model.User;
import com.mipt.wallet.model.Operation;
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
