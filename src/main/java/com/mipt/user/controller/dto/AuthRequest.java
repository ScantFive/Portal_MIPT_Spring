package com.mipt.user.controller.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String login;
    private String password;
}