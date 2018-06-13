package com.doitincloud.oauth2.revocations;

import com.doitincloud.oauth2.supports.AuthenticationContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RefreshTokenRevokeService extends AbstractRevocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenRevokeService.class);

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void revoke(String token) {

        OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(token);
        if (refreshToken == null) {
            LOGGER.trace("refresh_token not found: " + token);
            return;
        }

        OAuth2Authentication auth = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        if (auth == null) {
            tokenStore.removeRefreshToken(refreshToken);
            return;
        }

        AuthenticationContext context = new AuthenticationContext(clientDetailsService, userDetailsService);
        if (!revokeProcess(context, auth, null, refreshToken)) {
            throw new InsufficientAuthenticationException("insufficient authority to perform revocation");
        }
    }

    @Override
    public boolean supports(String type) {
        return "refresh_token".equals(type);
    }
}
