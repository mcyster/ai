package com.extole.app.jira;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.extole.app.jira.authentication.CustomOAuth2UserService;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final boolean isOauthEnabled;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, 
                          @Value("${oauth2.enabled:true}") boolean isOauthEnabled) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.isOauthEnabled = isOauthEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(new ForwardedHeaderFilter(), UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/**")
            );

        if (isOauthEnabled) {
            http.authorizeHttpRequests(authorizeRequests ->
                    authorizeRequests
                        .requestMatchers("/terms.html", "/privacy.html", "/ticket").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                    )
                );
        } else {
            http.authorizeHttpRequests(authorizeRequests ->
                    authorizeRequests
                        .anyRequest().permitAll()
            );
        }

        http.logout(logout -> logout
            .permitAll()
        );

        return http.build();
    }
}