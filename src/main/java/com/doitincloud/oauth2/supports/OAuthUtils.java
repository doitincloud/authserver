package com.doitincloud.oauth2.supports;

import com.doitincloud.oauth2.models.Client;
import com.doitincloud.oauth2.models.User;
import com.doitincloud.commons.Utils;

import com.doitincloud.oauth2.repositories.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;

import java.io.Serializable;
import java.util.*;

public class OAuthUtils {

    public static OAuth2Authentication createOAuth2Authentication(Map<String, Object> map, UserRepo userRepo) {

        Map<String, Object> requestMap = (Map<String, Object>) map.get("oauth2Request");
        OAuth2Request request = createOAuth2Request(requestMap);

        Map<String, Object> authenticationMap = (Map<String, Object>) map.get("userAuthentication");
        Authentication authentication = createUserAuthentication(authenticationMap, userRepo);

        return new OAuth2Authentication(request, authentication);
    }

    public static OAuth2Request createOAuth2Request(Map<String, Object> map) {
        //return Utils.toPojo(map, OAuth2Request.class);

        Map<String, String> parameters = (Map<String, String>) map.get("requestParameters");
        String clientId = (String) map.get("clientId");
        boolean approved = (Boolean) map.get("approved");
        String redirectUri = (String) map.get("redirectUri");;

        Collection<GrantedAuthority> authorities = getGrantedAuthorities((List<Map<String, String>>) map.get("authorities"));

        Set<String> scope = new HashSet<>((List<String>) map.get("scope"));
        Set<String> resourceIds = new HashSet<>((List<String>) map.get("resourceIds"));
        Set<String> responseTypes = new HashSet<>((List<String>) map.get("responseTypes"));
        Map<String, Serializable> extensions = (Map<String, Serializable>) map.get("extensions");

        OAuth2Request request = new OAuth2Request(parameters, clientId, authorities, approved, scope, resourceIds,
                redirectUri, responseTypes, extensions);

        Map<String, Object> refreshRequest = (Map<String, Object>) map.get("refreshTokenRequest");
        if (refreshRequest != null) {
            TokenRequest refreshTokenRequest = Utils.toPojo(refreshRequest, TokenRequest.class);
            request.refresh(refreshTokenRequest);
        }

        return request;
    }

    public static Authentication createUserAuthentication(Map<String, Object> map, UserRepo userRepo) {
        if (map == null) {
            return null;
        }
        return new AuthenticationImpl(map, userRepo);
    }

    public static Collection<GrantedAuthority> getGrantedAuthorities(List<Map<String, String>> list) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (list == null || list.size() == 0) {
            return authorities;
        }
        for (Map<String, String> item : list) {
            String role = item.get("authority");
            if (role == null) {
                continue;
            }
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    public static Collection<GrantedAuthority> getGrantedAuthorities(Set<String> set) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (set == null || set.size() == 0) {
            return authorities;
        }
        for (String role : set) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    public static Set<String> getAuthorities(Collection<? extends GrantedAuthority> grantedAuthorities) {
        Set<String> authorities = new HashSet<>();
        for (GrantedAuthority authority: grantedAuthorities) {
            authorities.add(authority.getAuthority());
        }
        return authorities;
    }

    public static ClientDetails toClientDetails(Client client) {
        return new ClientDetails() {
            @Override
            public String getClientId() {
                return client.getClientId();
            }

            @Override
            public Set<String> getResourceIds() {
                return client.getResourceIds();
            }

            @Override
            public boolean isSecretRequired() {
                return client.isSecretRequired();
            }

            @Override
            public String getClientSecret() {
                return client.getClientSecret();
            }

            @Override
            public boolean isScoped() {
                return client.isScoped();
            }

            @Override
            public Set<String> getScope() {
                return client.getScope();
            }

            @Override
            public Set<String> getAuthorizedGrantTypes() {
                return client.getAuthorizedGrantTypes();
            }

            @Override
            public Set<String> getRegisteredRedirectUri() {
                return client.getRegisteredRedirectUri();
            }

            @Override
            public Collection<GrantedAuthority> getAuthorities() {
                return getGrantedAuthorities(client.getAuthorities());
            }

            @Override
            public Integer getAccessTokenValiditySeconds() {
                return client.getAccessTokenValiditySeconds();
            }

            @Override
            public Integer getRefreshTokenValiditySeconds() {
                return client.getRefreshTokenValiditySeconds();
            }

            @Override
            public boolean isAutoApprove(String scope) {
                return client.isAutoApprove(scope);
            }

            @Override
            public Map<String, Object> getAdditionalInformation() {
                return client.getAdditionalInformation();
            }
        };
    }

    public static UserDetails toUserDetails(User user) {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return getGrantedAuthorities(user.getAuthorities());
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getUsername();
            }

            @Override
            public boolean isAccountNonExpired() {
                return user.isAccountNonExpired();
            }

            @Override
            public boolean isAccountNonLocked() {
                return user.isAccountNonLocked();
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return user.isCredentialsNonExpired();
            }

            @Override
            public boolean isEnabled() {
                return user.isEnabled();
            }
        };
    }
}
