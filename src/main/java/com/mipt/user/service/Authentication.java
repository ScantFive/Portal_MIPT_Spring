package com.mipt.user.service;

public interface Authentication {

  Boolean logIn(String email, String password);

  void forgotPassword(String email, String rawPassword1, String rawPassword2);
}
