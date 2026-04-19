package com.mipt.user.controller.dto;

import lombok.Data;

@Data
public class AuthByEmailRequest {
    private String email;
    private String password;
}