package com.doitincloud.oauth2.revocations;

import com.doitincloud.oauth2.supports.AuthenticationContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Service
public class RevokeByClientIdService extends AbstractRevocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeByClientIdService.class);

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void revoke(String clientId) {

        Collection<OAuth2AccessToken> accessTokens = tokenStore.findTokensByClientId(clientId);
        if (accessTokens == null || accessTokens.size() == 0) {
            LOGGER.trace("access_token not found for client " + clientId);
            return;
        }

        AuthenticationContext context = new AuthenticationContext(clientDetailsService, userDetailsService);
        for (OAuth2AccessToken accessToken : accessTokens) {

            OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
            OAuth2Authentication auth = tokenStore.readAuthentication(accessToken);
            if (auth == null) {
                tokenStore.removeAccessToken(accessToken);
                if (accessToken != null) {
                    tokenStore.removeAccessToken(accessToken);
                }
                if (refreshToken != null) {
                    tokenStore.removeRefreshToken(refreshToken);
                }
                continue;
            }

            if (!revokeProcess(context, auth, accessToken, refreshToken)) {
                throw new InsufficientAuthenticationException("insufficient authority to perform revocation");
            }
        }
    }

    public boolean supports(String type) {
        return "client_id".equals(type);
    }

}
