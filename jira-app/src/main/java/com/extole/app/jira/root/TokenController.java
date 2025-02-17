package com.extole.app.jira.root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private final boolean isSecure;

    public TokenController(@Value("${app.url}") String appUrl) {

        if (appUrl.startsWith("https")) {
            this.isSecure = true;
        } else {
            this.isSecure = false;
        }

    }

    @GetMapping("/app-token")
    @ResponseStatus(HttpStatus.FOUND)
    public void setTokenFromHeaderAndRedirect(@RequestParam String url, HttpServletRequest request,
            HttpServletResponse response) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            Cookie cookie = new Cookie("id_token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            if (isSecure) {
                cookie.setSecure(true);
            }
            cookie.setMaxAge(3600);

            response.addCookie(cookie);
        }

        response.setHeader("Location", url);
    }

    @GetMapping("/app-dump")
    @ResponseStatus(HttpStatus.OK)
    public void dumpToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("app-dump");

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                logger.info("cookie: {}: {}", cookie.getName(), cookie.getValue());
            }
        }
    }
}
