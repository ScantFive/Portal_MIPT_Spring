package com.mipt.user.repository;

import com.mipt.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<User> findByLogin(String login);

  boolean existsByLogin(String login);
}
