package com.extole.app.jira;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.extole.app.jira.authentication.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private String appUrl;
    
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, @Value("${app.url}") String appUrl) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.appUrl = appUrl;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ticket")
            )
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/terms.html", "/privacy.html", "/ticket").permitAll() 
                    .anyRequest().authenticated() 
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
            )
            .logout(logout -> logout
                .permitAll()
            );

            
        if (appUrl.startsWith("https")) {
            http.requiresChannel(channel -> channel
                .anyRequest().requiresSecure());
        }

        return http.build();
    }
    

}
