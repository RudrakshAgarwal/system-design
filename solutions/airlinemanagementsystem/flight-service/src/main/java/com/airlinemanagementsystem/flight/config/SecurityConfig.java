package com.airlinemanagementsystem.flight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow Swagger UI & API Docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Allow Public Access to Flight Search
                        .requestMatchers(HttpMethod.GET, "/api/v1/flights/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/airports/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/seats/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/aircrafts/**").permitAll()

                        // Allow public access to our Week 2 Simulation endpoint
                        .requestMatchers("/api/v1/simulation/**").permitAll()

                        // Everything else (e.g., POST/PUT/DELETE) requires Authentication
                        .anyRequest().authenticated()
                )
                // Uses the Keycloak settings from application.yml
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
