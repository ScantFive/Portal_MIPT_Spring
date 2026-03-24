package com.mipt.service;

import com.mipt.model.user.User;

public interface Authentification {

  Boolean logIn(String email, String rawPassword);

  void forgotPassword(String email, String rawPassword1, String rawPassword2);
}