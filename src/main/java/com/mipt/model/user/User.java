package com.mipt.model.user;

import com.mipt.service.util.PasswordHasher;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID userId;

  @Column(unique = true, nullable = false, length = 50)
  private String login;

  @Column(unique = true, nullable = false, length = 100)
  private String email;

  @Column(name = "hashed_password", nullable = false)
  private String hashedPassword;

  @Column(nullable = false)
  private Boolean activated = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public User(String login, String email, String rawPassword) {
    this.userId = UUID.randomUUID();
    this.login = login;
    this.email = email.toLowerCase().trim();
    this.hashedPassword = PasswordHasher.hash(rawPassword);
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  // Private constructor for database reconstruction
  private User(UUID userId, String login, String email, String hashedPassword,
               Boolean activated, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.userId = userId;
    this.login = login;
    this.email = email.toLowerCase().trim();
    this.hashedPassword = hashedPassword;
    this.activated = activated != null ? activated : false;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User fromDatabase(UUID userId, String login, String email,
                                  String passwordHash, Boolean activated,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
    return new User(userId, login, email, passwordHash, activated, createdAt, updatedAt);
  }

  public boolean checkPassword(String rawPassword) {
    return PasswordHasher.verify(rawPassword, this.hashedPassword);
  }

  public void changePassword(String rawPassword1, String rawPassword2) {
    if (rawPassword1.equals(rawPassword2) && rawPassword1.length() > 6) {
      this.hashedPassword = PasswordHasher.hash(rawPassword1);
      this.updatedAt = LocalDateTime.now();
    }
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
