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

                        // Статические ресурсы фронтенда
                        .requestMatchers("/", "/index.html", "/assets/**", "/*.svg", "/*.ico", "/*.js", "/*.css").permitAll()

                        // SPA-маршруты React Router
                        .requestMatchers("/login", "/register", "/activation-pending", "/activate",
                                "/catalog", "/listing/**", "/deals", "/messages", "/profile", "/wallet").permitAll()

                        // Публичные эндпоинты для авторизации и регистрации
                        .requestMatchers("/users/authenticate/**", "/users/activate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()

                        // Поиск по email и логину для процесса входа
                        .requestMatchers(HttpMethod.GET, "/users/by-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/by-login/**").permitAll()
<<<<<<< HEAD
                        .requestMatchers("/users/by-telegram/**").permitAll()
                        .requestMatchers("/users/{id}/telegram-chat").permitAll()
                        .requestMatchers(("/users/by-email")).permitAll()
                        .requestMatchers("/users/{id}/email").permitAll()
=======

                        // Внутренние вызовы notification-сервиса (без JWT)
                        .requestMatchers(HttpMethod.GET, "/users/by-telegram/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/*/telegram-chat").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/*/telegram-chat").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/*/telegram-chat").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/*/email").permitAll()

                        // Публичный просмотр контента
                        .requestMatchers(HttpMethod.GET, "/v1/advertisements/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/search/**").permitAll()
>>>>>>> 9181b64 (fix: Spring Security blocking api requests)

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