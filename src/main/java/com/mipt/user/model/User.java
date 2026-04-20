package com.mipt.user.model;

import com.mipt.util.PasswordHasher;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// Добавляем импорты Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable, UserDetails { // Добавляем интерфейс

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID userID;

    @Column(name = "login", nullable = false, unique = true, length = 255)
    private String login;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @Column(name = "activated", nullable = false)
    private Boolean activated = false;

    // --- Твои существующие методы остаются без изменений ---

    public User(String login, String email, String encodedPassword) {
        this.userID = UUID.randomUUID();
        this.login = login;
        this.email = email.toLowerCase().trim();
        this.hashedPassword = encodedPassword;
        this.activated = false;
    }

    // --- Реализация методов UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Возвращаем базовую роль для всех пользователей
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.hashedPassword; // Spring Security будет использовать это поле для сверки
    }

    @Override
    public String getUsername() {
        return this.email; // Используем email как главный идентификатор (логин)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Можно привязать к твоему полю activated
        return this.activated != null && this.activated;
    }

    public boolean checkPassword(String rawPassword) {
        return PasswordHasher.verify(rawPassword, this.hashedPassword);
    }
}