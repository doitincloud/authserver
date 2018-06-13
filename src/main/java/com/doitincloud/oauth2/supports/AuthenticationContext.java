package com.doitincloud.oauth2.supports;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.Set;

public class AuthenticationContext {

    private ClientDetailsService clientDetailsService;

    private UserDetailsService userDetailsService;

    private OAuth2Authentication currentAuth;

    private String currentAccessToken;

    private String currentUsername = "";

    private String currentClientId;

    private Set<String> currentUserAuhorities;

    private Set<String> currentClientAuhorities;

    public AuthenticationContext(ClientDetailsService clientDetailsService, UserDetailsService userDetailsService) {
        this.clientDetailsService = clientDetailsService;
        this.userDetailsService = userDetailsService;
    }

    public OAuth2Authentication getCurrentAuth() {
        if (currentAuth == null) {
            currentAuth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        }
        return currentAuth;
    }

    public String getCurrentAccessToken() {
        if (currentAccessToken == null) {
            Object object = getCurrentAuth().getDetails();
            if (object != null && object instanceof OAuth2AuthenticationDetails) {
                OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) getCurrentAuth().getDetails();
                currentAccessToken = details.getTokenValue();
            }
        }
        return currentAccessToken;
    }

    public String getCurrentUsername() {
        if (currentUsername != null && currentUsername.length() == 0) {
            currentUsername = getUsername(getCurrentAuth());
        }
        return currentUsername;
    }

    public String getCurrentClientId() {
        if (currentClientId == null) {
            currentClientId = getClientId(getCurrentAuth());
        }
        return currentClientId;
    }

    public Set<String> getCurrentUserAuhorities() {
        if (currentUserAuhorities == null) {
            currentUserAuhorities = getUserAuthorities(getCurrentAuth());
        }
        return currentUserAuhorities;
    }

    public Set<String> getCurrentClientAuhorities() {
        if (currentClientAuhorities == null) {
            currentClientAuhorities = getClientAuthorities(getCurrentAuth());
        }
        return currentClientAuhorities;
    }

    public String getUsername(OAuth2Authentication auth) {
        if (auth.isClientOnly()) {
            return null;
        }
        return auth.getName();
    }

    public String getClientId(OAuth2Authentication auth) {
        if (auth.isClientOnly()) {
            return auth.getName();
        }
        return auth.getOAuth2Request().getClientId();
    }

    public UserDetails getUserDetails(OAuth2Authentication auth) {
        String username = getUsername(auth);
        if (username == null) {
            return null;
        }
        return userDetailsService.loadUserByUsername(username);
    }

    public ClientDetails getClientDetails(OAuth2Authentication auth) {
        return clientDetailsService.loadClientByClientId(getClientId(auth));
    }

    public Set<String> getUserAuthorities(OAuth2Authentication auth) {
        UserDetails userDetails = getUserDetails(auth);
        if (userDetails == null) {
            return null;
        }
        return OAuthUtils.getAuthorities(userDetails.getAuthorities());
    }

    public Set<String> getClientAuthorities(OAuth2Authentication auth) {
        ClientDetails clientDetails = getClientDetails(auth);
        return OAuthUtils.getAuthorities(clientDetails.getAuthorities());
    }
}