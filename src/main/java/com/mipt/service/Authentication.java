package com.mipt.service;

import com.mipt.model.user.User;

public interface Authentication {

  Boolean logIn(String email, String rawPassword);

  void forgotPassword(String email, String rawPassword1, String rawPassword2);

  default User register(String login, String email, String password, String confirmPassword) {
    throw new UnsupportedOperationException("Not implemented");
  }

  default boolean changePassword(User user, String oldPassword, String newPassword, String confirmPassword) {
    throw new UnsupportedOperationException("Not implemented");
  }
}