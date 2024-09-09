package com.extole.app.jira.authentication;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.extole.app.jira.root.RootController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger logger = LogManager.getLogger(RootController.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        logger.info("Login - " + oauth2User.toString());
        
        String email = null;
        Boolean emailVerified = Boolean.FALSE;

        if (oauth2User instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) oauth2User;
            email = oidcUser.getEmail();
            emailVerified = oidcUser.getEmailVerified();
        } else {
            email = (String) oauth2User.getAttributes().get("email");
            emailVerified = (Boolean) oauth2User.getAttributes().get("email_verified");
        }

        if (email == null || emailVerified == null || !emailVerified) {
            throw new AuthenticationException("Email is not verified") {};
        }
        
        if (!email.equals("mcyster@gmail.com") && !email.endsWith("@extole.com")) {
            throw new AuthenticationException("Unauthorized email" + email) {};
        }

        return oauth2User;
    }
}
