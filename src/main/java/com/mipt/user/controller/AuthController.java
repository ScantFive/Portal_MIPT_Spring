package com.mipt.user.controller;

import com.mipt.user.controller.dto.AuthRequest;
import com.mipt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/authenticate")
    public boolean authenticate(@RequestBody AuthRequest request) {
        return userService.authenticate(request.getLogin(), request.getPassword());
    }

    @GetMapping("/by-login/{login}")
    public String getUserId(@PathVariable String login) {
        return userService.findByLogin(login)
                .map(user -> user.getUserID().toString())
                .orElse(null);
    }
}