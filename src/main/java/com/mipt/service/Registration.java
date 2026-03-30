package com.mipt.service;

public interface Registration {

  void register(String login, String email, String password);

  void verification(String email);

  void toProfile();
}
