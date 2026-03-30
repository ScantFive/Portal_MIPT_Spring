package com.mipt.user.service;

public interface Registration {

  void register(String login, String email, String password);

  void verification(String email);

  void toProfile();
}
