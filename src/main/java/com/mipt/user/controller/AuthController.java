package com.mipt.user.controller;

import com.mipt.user.controller.dto.AuthByEmailRequest;
import com.mipt.user.controller.dto.AuthRequest;
import com.mipt.security.JwtService;
import com.mipt.security.UserDetailsImpl;
import com.mipt.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        return processAuth(request.getLogin(), request.getPassword());
    }

    @PostMapping("/authenticate/email")
    public ResponseEntity<?> authenticateByEmail(@RequestBody AuthByEmailRequest request) {
        return processAuth(request.getEmail(), request.getPassword());
    }

    private ResponseEntity<?> processAuth(String identifier, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, password)
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                    "token", jwtToken,
                    "email", userDetails.getUsername()
            ));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Аккаунт не активирован. Проверьте почту и перейдите по ссылке активации."));
        } catch (Exception e) {
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