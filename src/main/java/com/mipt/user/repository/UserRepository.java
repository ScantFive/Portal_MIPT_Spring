package com.mipt.user.repository;

import com.mipt.user.model.User;
import com.mipt.util.SpringContext;
import java.util.*;

public class UserRepository {

  private UserJpaRepository repository() {
    return SpringContext.getBean(UserJpaRepository.class);
  }

  public void save(User user) {
    repository().save(user);
  }

  public boolean existsByEmail(String email) {
    return repository().existsByEmail(email.toLowerCase().trim());
  }

  public Optional<User> findByEmail(String email) {
    return repository().findByEmail(email.toLowerCase().trim());
  }

  public Optional<User> findById(UUID id) {
    return repository().findById(id);
  }

  public void update(User user) {
    repository().save(user);
  }

  public List<User> findAll() {
    return repository().findAll();
  }

  public boolean deleteById(UUID id) {
    if (!repository().existsById(id)) {
      return false;
    }
    repository().deleteById(id);
    return true;
  }

  public void clear() {
    repository().deleteAll();
  }
}
