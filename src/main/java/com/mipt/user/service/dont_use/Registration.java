package com.mipt.user.service.dont_use;

import com.mipt.user.model.User;

public interface Registration {

  void register(User user);

  void verification(String email);

  void toProfile();
}
