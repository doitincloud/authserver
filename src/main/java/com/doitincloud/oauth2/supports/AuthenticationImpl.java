package com.doitincloud.oauth2.supports;

import com.doitincloud.oauth2.models.User;
import com.doitincloud.oauth2.repositories.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AuthenticationImpl implements Authentication {

    private String name;

    private Collection<GrantedAuthority> authorities;

    private Object credentials;

    private Object details;

    private String principal;

    boolean authenticated;

    public AuthenticationImpl(Map<String, Object> map, UserRepo userRepo) {
        name = (String) map.get("name");
        credentials = map.get("credentials");
        details = map.get("details");
        authenticated = (boolean) map.get("authenticated");
        Object object = map.get("principal");
        if (object instanceof Map) {
            Map<String, Object> mapPrincipal = (Map<String, Object>) object;
            principal = (String) mapPrincipal.get("username");
        } else if (object instanceof String) {
            principal = (String) object;
        }
        if (principal == null) {
            principal = name;
        }
        object = map.get("authorities");
        if (object != null && object instanceof List) {
            List<Map<String, String>> list = (List<Map<String, String>>) object;
            if (list.size() > 0) {
                authorities = OAuthUtils.getGrantedAuthorities(list);
            }
        }
        if (authorities == null) {
            User user = userRepo.findByUsername(principal);
            if (user != null) {
                authorities = OAuthUtils.getGrantedAuthorities(user.getAuthorities());
            }
        }
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return name;
    }
}
