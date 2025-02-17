package com.extole.app.jira;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieJwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CookieJwtAuthenticationFilter.class);

    private final JwtDecoder jwtDecoder;

    public CookieJwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            jakarta.servlet.FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("id_token".equals(cookie.getName())) {
                    try {
                        String token = cookie.getValue();
                        logger.debug("Received id_token: {}", token);
                        Jwt jwt = jwtDecoder.decode(token);
                        Authentication authentication = new JwtAuthenticationConverter().convert(jwt);
                        if (authentication != null) {
                            logger.info("Setting Spring Security context for user: {}", jwt.getSubject());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            logger.warn("Failed to create authentication object from JWT");
                        }
                    } catch (Exception e) {
                        logger.error("JWT decoding failed", e);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
