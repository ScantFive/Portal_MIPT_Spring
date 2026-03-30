package com.mipt.user.service;

import com.mipt.user.model.User;
import com.mipt.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserJpaRepository repository;

  public void save(User user) {
    if (user.getUserID() == null) {
      user.setUserID(UUID.randomUUID());
    }
    repository.save(user);
  }

  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email.toLowerCase().trim());
  }

  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email.toLowerCase().trim());
  }

  public Optional<User> findById(UUID id) {
    return repository.findById(id);
  }

  public void update(User user) {
    repository.save(user);
  }

  public List<User> findAll() {
    return repository.findAll();
  }

  public boolean deleteById(UUID id) {
    if (repository.existsById(id)) {
      repository.deleteById(id);
      return true;
    }
    return false;
  }

  public void clear() {
    repository.deleteAll();
  }
}
