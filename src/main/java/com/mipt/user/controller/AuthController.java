package com.mipt.user.controller;

import com.mipt.user.controller.dto.AuthByEmailRequest;
import com.mipt.user.controller.dto.AuthRequest;
import com.mipt.user.service.UserService;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/authenticate/email")
    public ResponseEntity<?> authenticateByEmail(@RequestBody AuthByEmailRequest request) {
        boolean success = userService.authenticateByEmail(request.getEmail(),
                request.getPassword());

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "token", "fake-jwt-token-for-now",
                    "email", request.getEmail()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/by-login/{login}")
    public String getUserId(@PathVariable String login) {
        return userService.findByLogin(login)
                .map(user -> user.getUserID().toString())
                .orElse(null);
    }
}