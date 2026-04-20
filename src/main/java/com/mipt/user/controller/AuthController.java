package com.mipt.user.controller;

import com.mipt.frontend.security.JwtUtils;
import com.mipt.user.controller.dto.AuthByEmailRequest;
import com.mipt.user.controller.dto.AuthRequest;
import com.mipt.user.model.User;
import com.mipt.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/authenticate")
    public boolean authenticate(@RequestBody AuthRequest request) {
        return userService.authenticate(request.getLogin(), request.getPassword());
    }


    @PostMapping("/authenticate/email")
    public ResponseEntity<?> authenticateByEmail(@RequestBody AuthByEmailRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "userId", user.getUserID(),
                "email", user.getEmail()
        ));
    }

    @GetMapping("/by-login/{login}")
    public String getUserId(@PathVariable String login) {
        return userService.findByLogin(login)
                .map(user -> user.getUserID().toString())
                .orElse(null);
    }
}