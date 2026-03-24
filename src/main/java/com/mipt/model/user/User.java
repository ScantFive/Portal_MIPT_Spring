package com.mipt.model.user;

import com.mipt.service.util.PasswordHasher;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements Serializable {

  @Id
  private UUID userID;
  private String login;
  private String email;
  private String hashedPassword;
  private Boolean activated = false;

  public User(String login, String email, String rawPassword) {
    this.userID = UUID.randomUUID();
    this.login = login;
    this.email = email.toLowerCase().trim();
    this.hashedPassword = PasswordHasher.hash(rawPassword);
  }

  private User(UUID userID, String login, String email, String hashedPassword, Boolean activated) {
    this.userID = userID;
    this.login = login;
    this.email = email.toLowerCase().trim();
    this.hashedPassword = hashedPassword;
    this.activated = activated != null ? activated : false;
  }

  public static User fromDatabase(UUID userID, String login, String email, String passwordHash, Boolean activated) {
    return new User(userID, login, email, passwordHash, activated);
  }

  public boolean checkPassword(String rawPassword) {
    return PasswordHasher.verify(rawPassword, this.hashedPassword);
  }

  public void changePassword(String rawPassword1, String rawPassword2) {
    if (rawPassword1.equals(rawPassword2) && rawPassword1.length() > 6) {
      this.hashedPassword = PasswordHasher.hash(rawPassword1);
    }
  }
}
