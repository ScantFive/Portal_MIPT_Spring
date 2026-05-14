package com.mipt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Применяем нашу настройку CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Внутри метода securityFilterChain
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Публичные эндпоинты для авторизации и регистрации
                        .requestMatchers("/users/authenticate/**", "/users/activate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()

                        // РАЗРЕШАЕМ ПОИСК ПО EMAIL И ЛОГИНУ ДЛЯ ПРОЦЕССА ВХОДА[cite: 17, 18]
                        .requestMatchers(HttpMethod.GET, "/users/by-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/by-login/**").permitAll()
                        .requestMatchers("/users/by-telegram/**").permitAll()
                        .requestMatchers("/users/{id}/telegram-chat").permitAll()
                        .requestMatchers(("/users/by-email")).permitAll()
                        .requestMatchers("/users/{id}/email").permitAll()

                        // Все остальные запросы требуют JWT токен
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // НОВЫЙ БИН ДЛЯ НАСТРОЙКИ CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Разрешаем адрес вашего фронтенда
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // Разрешаем все стандартные методы[cite: 19]
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Разрешаем заголовок Authorization для передачи JWT и Content-Type для JSON[cite: 19]
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}