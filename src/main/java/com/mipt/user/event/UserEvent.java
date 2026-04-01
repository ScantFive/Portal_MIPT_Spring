package com.mipt.user.event;

import com.mipt.user.model.User;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
 private String eventType;
 private UUID userId;
 private String username;
 private String email;
 private String action;
 private String details;
 private Instant timestamp;

 public static UserEvent registered(User user) {
  return base("USER_REGISTERED", user, "REGISTER", "User registered");
 }

 public static UserEvent updated(User user) {
  return base("USER_UPDATED", user, "UPDATE", "User profile updated");
 }

 public static UserEvent deleted(User user) {
  return base("USER_DELETED", user, "DELETE", "User deleted");
 }

 private static UserEvent base(String type, User user, String action, String details) {
  return UserEvent.builder()
    .eventType(type)
    .userId(user.getUserID())
    .username(user.getLogin())
    .email(user.getEmail())
    .action(action)
    .details(details)
    .timestamp(Instant.now())
    .build();
 }
}
