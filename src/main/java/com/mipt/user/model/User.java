package com.mipt.user.model;

import com.mipt.util.PasswordHasher;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "user_id")
  private UUID userID;

  @Column(name = "login", nullable = false, unique = true, length = 255)
  private String login;

  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "hashed_password", nullable = false, length = 255)
  private String hashedPassword;

  @Column(name = "activated", nullable = false)
  private Boolean activated = false;

  /**
   * Factory constructor for creating a new user with raw password.
   * This constructor generates a new UUID and hashes the password.
   */
  public User(String login, String email, String rawPassword) {
    this.userID = UUID.randomUUID();
    this.login = login;
    this.email = email.toLowerCase().trim();
    this.hashedPassword = PasswordHasher.hash(rawPassword);
    this.activated = false;
  }

  public static User fromDatabase(
      UUID userID,
      String login,
      String email,
      String passwordHash,
      Boolean activated) {
    User user = new User();
    user.userID = userID;
    user.login = login;
    user.email = email.toLowerCase().trim();
    user.hashedPassword = passwordHash;
    user.activated = activated != null ? activated : false;
    return user;
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