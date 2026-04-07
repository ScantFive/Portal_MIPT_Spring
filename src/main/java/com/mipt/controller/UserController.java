package com.mipt.controller;

import com.mipt.user.model.User;
import com.mipt.user.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(
    origins = {"http://localhost:3000"},
    allowedHeaders = "*",
    methods = {
        org.springframework.web.bind.annotation.RequestMethod.GET,
        org.springframework.web.bind.annotation.RequestMethod.POST,
        org.springframework.web.bind.annotation.RequestMethod.PUT,
        org.springframework.web.bind.annotation.RequestMethod.DELETE,
        org.springframework.web.bind.annotation.RequestMethod.OPTIONS,
        org.springframework.web.bind.annotation.RequestMethod.PATCH
    },
    allowCredentials = "true",
    maxAge = 3600
)
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

 @GetMapping("/by-email")
 public User getByEmail(@RequestParam String email) {
  return userService
    .findByEmail(email)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 }

 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public User create(@RequestBody CreateUserRequest request) {
  if (userService.existsByEmail(request.email())) {
   throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
  }
  User user = new User(request.login(), request.email(), request.password());
  userService.save(user);
  return user;
 }

 @PutMapping("/{id}")
 public User update(@PathVariable UUID id, @RequestBody User user) {
  if (userService.findById(id).isEmpty()) {
   throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
  }
  user.setUserID(id);
  userService.update(user);
  return user;
 }

 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@PathVariable UUID id) {
  if (!userService.deleteById(id)) {
   throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
  }
 }

 public record CreateUserRequest(String login, String email, String password) {
 }
}
