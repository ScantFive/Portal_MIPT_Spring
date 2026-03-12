package com.mipt.service.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordHasher {

  // Хэширует пароль с автоматической генерацией соли
  public static String hash(String rawPassword) {
    if (rawPassword == null || rawPassword.isEmpty()) {
      throw new IllegalArgumentException("Пароль не может быть пустым");
    }
    return BCrypt.withDefaults().hashToString(10, rawPassword.toCharArray());
  }

  public static boolean verify(String rawPassword, String hashedPassword) {
    if (rawPassword == null || hashedPassword == null) {
      return false;
    }
    return BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword).verified;
  }
}
