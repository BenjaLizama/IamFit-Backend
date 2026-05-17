package com.iam_fit.ms_rutinas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/v1/chat/**")
                        .authenticated()
                        .requestMatchers("/api/libros/**")
                        .authenticated()

                        .anyRequest()
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

}
