package com.mipt.user.service;

import com.mipt.user.event.UserEvent;
import com.mipt.user.model.User;
import com.mipt.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserJpaRepository repository;
  private final UserKafkaEventPublisher eventPublisher;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public void save(User user) {
    if (user.getUserID() == null) {
      user.setUserID(UUID.randomUUID());
    }
    String token = UUID.randomUUID().toString();
    user.setActivationToken(token);
    User saved = repository.save(user);
    String activationUrl = baseUrl + "/api/activate?token=" + token;
    log.info("Activation URL for {}: {}", saved.getEmail(), activationUrl);
    eventPublisher.publish(UserEvent.registered(saved, activationUrl));
  }

  public boolean activate(String token) {
    return repository.findByActivationToken(token).map(user -> {
      user.setActivated(true);
      user.setActivationToken(null);
      repository.save(user);
      return true;
    }).orElse(false);
  }

  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email.toLowerCase().trim());
  }

  public boolean existsByLogin(String login) {
    return repository.existsByLogin(login.toLowerCase().trim());
  }

  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email.toLowerCase().trim());
  }
  public Optional<User> findByLogin(String login){
    return repository.findByLogin(login.toLowerCase().trim());
  }

  public Optional<User> findById(UUID id) {
    return repository.findById(id);
  }

  public void update(User user) {
    User updated = repository.save(user);
    eventPublisher.publish(UserEvent.updated(updated));
  }

  public List<User> findAll() {
    return repository.findAll();
  }

  public boolean deleteById(UUID id) {
    User existing = repository.findById(id).orElse(null);
    if (existing != null) {
      repository.deleteById(id);
      eventPublisher.publish(UserEvent.deleted(existing));
      return true;
    }
    return false;
  }
  public boolean authenticate(String login, String rawPassword){
    if (repository.existsByLogin(login)) {
      Optional<User> userOpt = repository.findByLogin(login);
      return userOpt.map(user -> user.checkPassword(rawPassword)).orElse(false);
    } else {
      return false;
    }
  }
  public boolean authenticateByEmail(String email, String rawPassword){
    if (repository.existsByEmail(email)) {
      Optional<User> userOpt = repository.findByEmail(email);
      return userOpt.map(user -> user.checkPassword(rawPassword)).orElse(false);
    } else {
      return false;
    }
  }
}
