package com.mipt.repository.user;

import com.mipt.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);
  Optional<User> findByLogin(String login);
  boolean existsByEmail(String email);
  boolean existsByLogin(String login);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.activated = true WHERE u.userID = :userId")
  int activateUser(@Param("userId") UUID userId);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.hashedPassword = :newPassword WHERE u.userID = :userId")
  int updatePassword(@Param("userId") UUID userId, @Param("newPassword") String newPassword);


  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.login = :newLogin, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userID = :userId")
  int updateLogin(@Param("userId") UUID userId, @Param("newLogin") String newLogin);

  /**
   * Обновление email пользователя
   */
  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.email = :newEmail, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userID = :userId")
  int updateEmail(@Param("userId") UUID userId, @Param("newEmail") String newEmail);

  /**
   * Обновление логина и email одновременно
   */
  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.login = :newLogin, u.email = :newEmail, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userID = :userId")
  int updateLoginAndEmail(@Param("userId") UUID userId,
                          @Param("newLogin") String newLogin,
                          @Param("newEmail") String newEmail);

  /**
   * Безопасное обновление логина с проверкой уникальности
   * (этот метод только обновляет, уникальность нужно проверять отдельно)
   */
  @Modifying
  @Transactional
  default int updateLoginSafe(UUID userId, String newLogin) {
    // Проверяем, что новый логин не занят другим пользователем
    Optional<User> existingUser = findByLogin(newLogin);
    if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
      throw new RuntimeException("Login already taken: " + newLogin);
    }
    return updateLogin(userId, newLogin);
  }

  /**
   * Безопасное обновление email с проверкой уникальности
   */
  @Modifying
  @Transactional
  default int updateEmailSafe(UUID userId, String newEmail) {
    // Проверяем, что новый email не занят другим пользователем
    Optional<User> existingUser = findByEmail(newEmail);
    if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
      throw new RuntimeException("Email already in use: " + newEmail);
    }
    return updateEmail(userId, newEmail);
  }
}
