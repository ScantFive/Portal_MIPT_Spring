package com.mipt.user.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotNull(message = "Введите логин")
    @Size(min = 3, message = "логин слишком короткий")
    private String login;

    @Pattern(regexp = ".*@phystech\\.edu$", message = "Разрешена только физтех почта")
    private String email;

    @NotNull(message = "Введите пароль")
    @Size(min = 6, message = "Пароль слишком короткий")
    private String password;
}
