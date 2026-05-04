package com.mipt.user.controller;

import com.mipt.user.controller.dto.CreateUserRequest;
import com.mipt.user.model.User;
import com.mipt.user.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

 private final UserService userService;

 @GetMapping
 public List<User> list() {
  return userService.findAll();
 }

 @GetMapping("/{id}")
 public User getById(@PathVariable UUID id) {
  return userService
    .findById(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 }

 @GetMapping("/{id}/email")
 public String getEmailById(@PathVariable UUID id) {
  return userService
    .findById(id)
    .map(User::getEmail)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 }

 @GetMapping("/by-telegram/{username}")
 public ResponseEntity<String> getByTelegramUsername(@PathVariable String username) {
  return userService.findByTelegramUsername(username)
    .map(u -> ResponseEntity.ok(u.getUserID().toString()))
    .orElse(ResponseEntity.notFound().build());
 }

 @GetMapping("/by-email")
 public User getByEmail(@RequestParam String email) {
  return userService
    .findByEmail(email)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 }

 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public User create(@RequestBody CreateUserRequest request) {
  if (userService.existsByEmail(request.getEmail())) {
   throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
  }
  if (userService.existsByLogin(request.getLogin())) {
   throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким логином уже существует");
  }
  User user = new User(request.getLogin(), request.getEmail(), request.getPassword());
  if (request.getTelegramUsername() != null && !request.getTelegramUsername().isBlank()) {
   String tg = request.getTelegramUsername().strip();
   user.setTelegramUsername(tg.startsWith("@") ? tg.substring(1) : tg);
  }
  userService.save(user);
  return user;
 }

 @PutMapping("/{id}")
 public User update(@PathVariable UUID id, @RequestBody User user) {
  if (userService.findById(id).isEmpty()) {
   throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
  }
  user.setUserID(id);
  if (user.getTelegramUsername() != null) {
   String tg = user.getTelegramUsername().strip();
   user.setTelegramUsername(tg.startsWith("@") ? tg.substring(1).toLowerCase() : tg.toLowerCase());
   if (user.getTelegramUsername().isBlank()) user.setTelegramUsername(null);
  }
  userService.update(user);
  return user;
 }

 @PostMapping("/{id}/telegram-chat")
 public ResponseEntity<Void> saveTelegramChatId(@PathVariable UUID id, @RequestParam Long chatId) {
  User user = userService.findById(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  user.setTelegramChatId(chatId);
  userService.update(user);
  return ResponseEntity.noContent().build();
 }

 @DeleteMapping("/{id}/telegram-chat")
 public ResponseEntity<Void> removeTelegramChatId(@PathVariable UUID id) {
  User user = userService.findById(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  user.setTelegramChatId(null);
  userService.update(user);
  return ResponseEntity.noContent().build();
 }

 @GetMapping("/{id}/telegram-chat")
 public ResponseEntity<Long> getTelegramChatId(@PathVariable UUID id) {
  User user = userService.findById(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  if (user.getTelegramChatId() == null) {
   return ResponseEntity.notFound().build();
  }
  return ResponseEntity.ok(user.getTelegramChatId());
 }

 @GetMapping("/activate")
 public ResponseEntity<?> activate(@RequestParam String token) {
  boolean ok = userService.activate(token);
  if (ok) {
   return ResponseEntity.ok(java.util.Map.of("message", "Аккаунт успешно активирован"));
  }
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный или уже использованный токен активации");
 }

 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@PathVariable UUID id) {
  if (!userService.deleteById(id)) {
   throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
  }
 }

}
