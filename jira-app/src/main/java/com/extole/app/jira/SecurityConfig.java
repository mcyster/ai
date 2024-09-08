package com.extole.app.jira;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Allow anonymous access to these specific URLs
                    .requestMatchers("/terms.html", "/privacy.html").permitAll()
                    // Require authentication for all other requests
                    .anyRequest().authenticated()
            )
            .oauth2Login(); // Use oauth2Login for authentication

        return http.build();
    }
}
