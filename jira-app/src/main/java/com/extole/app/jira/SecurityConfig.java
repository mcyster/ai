package com.extole.app.jira;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.extole.app.jira.authentication.CustomOAuth2UserService;

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
                .csrf(AbstractHttpConfigurer::disable);

        if (isOauthEnabled) {
            http.authorizeHttpRequests(
                    auth -> auth.requestMatchers("/terms.html", "/privacy.html", "/ticket").permitAll()
                            .requestMatchers("/conversations/messages").authenticated().anyRequest().authenticated())
                    .oauth2Login(oauth2 -> oauth2
                            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
                    .oauth2ResourceServer(
                            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        http.logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://accounts.google.com");
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Optionally, set a custom authority converter here if needed.
        return converter;
    }
}