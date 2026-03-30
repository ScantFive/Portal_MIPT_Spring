package com.mipt.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

  @Test
  void hashAndVerifyShouldWork() {
    String raw = "StrongPassword123";

    String hashed = PasswordHasher.hash(raw);

    assertNotNull(hashed);
    assertNotEquals(raw, hashed);
    assertTrue(PasswordHasher.verify(raw, hashed));
    assertFalse(PasswordHasher.verify("wrong", hashed));
  }

  @Test
  void hashShouldRejectBlankPassword() {
    assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
    assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
  }

  @Test
  void verifyShouldReturnFalseOnNullInput() {
    assertFalse(PasswordHasher.verify(null, "hash"));
    assertFalse(PasswordHasher.verify("raw", null));
  }
}
