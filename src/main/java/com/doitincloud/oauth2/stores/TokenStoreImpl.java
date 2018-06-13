package com.doitincloud.oauth2.stores;

import com.doitincloud.oauth2.repositories.LinkRepo;
import com.doitincloud.oauth2.repositories.TokenRepo;
import com.doitincloud.oauth2.repositories.UserRepo;
import com.doitincloud.oauth2.supports.AuthKeyGenerator;
import com.doitincloud.oauth2.supports.LinkToAccessToken;
import com.doitincloud.oauth2.supports.LinkToAuthentication;
import com.doitincloud.oauth2.supports.LinkToRefreshToken;
import com.doitincloud.oauth2.supports.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * originated from 
 * https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/InMemoryTokenStore.java
 *
 */
public class TokenStoreImpl implements TokenStore {

    private static final int DEFAULT_FLUSH_INTERVAL = 1000;

    private AccessTokenStore accessTokenStore;

    private RefreshTokenStore refreshTokenStore;

    private AuthenticationStore authenticationStore;

    private LinkToAccessToken authenticationToAccessToken;

    private LinkToAccessToken refreshTokenToAccessToken;

    private LinkToRefreshToken accessTokenToRefreshToken;

    private LinkToAuthentication accessTokenToAuthentication;

    private LinkToAuthentication refreshTokenToAuthentication;

    private LinkToAccessToken userNameToAccessToken;

    private LinkToAccessToken clientIdToAccessToken;


    public TokenStoreImpl(TokenRepo tokenRepo, LinkRepo linkRepo, UserRepo userRepo) {

        accessTokenStore = new AccessTokenStore(tokenRepo);

        refreshTokenStore = new RefreshTokenStore(tokenRepo);

        authenticationStore = new AuthenticationStore(tokenRepo, userRepo);

        authenticationToAccessToken = new LinkToAccessToken(tokenRepo, linkRepo, "Authentication/OAuth2AccessToken");

        refreshTokenToAccessToken = new LinkToAccessToken(tokenRepo, linkRepo, "RefreshToken/OAuth2AccessToken");

        accessTokenToRefreshToken = new LinkToRefreshToken(tokenRepo, linkRepo, "AccessToken/OAuth2RefreshToken");

        accessTokenToAuthentication = new LinkToAuthentication(tokenRepo, linkRepo, userRepo,"AccessToken/OAuth2Authentication");

        refreshTokenToAuthentication = new LinkToAuthentication(tokenRepo, linkRepo, userRepo,"RefreshToken/OAuth2Authentication");

        userNameToAccessToken = new LinkToAccessToken(tokenRepo, linkRepo, "UserName/OAuth2AccessToken", true);

        clientIdToAccessToken = new LinkToAccessToken(tokenRepo, linkRepo, "ClientId/OAuth2AccessToken", true);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String authenticationKey = AuthKeyGenerator.extractKey(authentication);
        OAuth2AccessToken accessToken = authenticationToAccessToken.get(authenticationKey);
        if (accessToken != null) {
            // Keep the stores consistent (maybe the same user is represented by this authentication but the details
            // have changed)
            OAuth2Authentication auth = readAuthentication(accessToken);
            String authKey = AuthKeyGenerator.extractKey(auth);
            if (!authenticationKey.equals(authKey)) {
                storeAccessToken(accessToken, authentication);
            }
        }
        return accessToken;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken accessToken) {
        String accessTokenKey = accessToken.getValue();
        return accessTokenToAuthentication.get(accessTokenKey);
    }

    @Override
    public OAuth2Authentication readAuthentication(String accessTokenKey) {
        return accessTokenToAuthentication.get(accessTokenKey);
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        String refeshTokenKey = token.getValue();
        return refreshTokenToAuthentication.get(refeshTokenKey);
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        accessTokenStore.put(accessToken);
        String authenticationKey = authenticationStore.put(authentication);
        authenticationToAccessToken.put(authenticationKey, accessToken);
        String accessTokenKey = accessToken.getValue();
        accessTokenToAuthentication.put(accessTokenKey, authentication);
        if (!authentication.isClientOnly()) {
            String approvalKey = getApprovalKey(authentication);
            userNameToAccessToken.put(approvalKey, accessToken);
        }
        String clientId = authentication.getOAuth2Request().getClientId();
        clientIdToAccessToken.put(clientId, accessToken);
        OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
        if ( refreshToken != null && refreshToken.getValue() != null) {
            String refreshTokenKey = refreshToken.getValue();
            refreshTokenToAccessToken.put(refreshTokenKey, accessToken);
            accessTokenToRefreshToken.put(accessTokenKey, refreshToken);
        }
    }

    private String getApprovalKey(OAuth2Authentication authentication) {
        Authentication userAuth = authentication.getUserAuthentication();
        String username = (userAuth == null ? "" : userAuth.getName());
        String clientId = authentication.getOAuth2Request().getClientId();
        return getApprovalKey(clientId, username);
    }

    private String getApprovalKey(String clientId, String username) {
        return clientId + (username==null ? "" : ":" + username);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken accessToken) {
        String accessTokenKey = accessToken.getValue();
        removeAccessToken(accessTokenKey);
    }

    private void removeAccessToken(String accessTokenKey) {
        OAuth2AccessToken removedAccessToken = accessTokenStore.remove(accessTokenKey);
        String refreshTokenKey = accessTokenToRefreshToken.remove(accessTokenKey);
        refreshTokenToAccessToken.remove(refreshTokenKey);
        String authenticationKey = accessTokenToAuthentication.remove(accessTokenKey);
        // Don't remove the refresh token and authentication - it's up to the caller to do that
        OAuth2Authentication authentication = this.authenticationStore.get(authenticationKey);
        if (authentication != null) {
            authenticationToAccessToken.remove(authenticationKey);
            String clientId = authentication.getOAuth2Request().getClientId();
            clientIdToAccessToken.removeToken(clientId, removedAccessToken);
            String username = authentication.getName();
            String approvalKey = getApprovalKey(clientId, username);
            userNameToAccessToken.removeToken(approvalKey, removedAccessToken);
        }
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessTokenKey) {
        return accessTokenStore.get(accessTokenKey);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        refreshTokenStore.put(refreshToken);
        String refreshTokenKey = refreshToken.getValue();
        refreshTokenToAuthentication.put(refreshTokenKey, authentication);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String refreshTokenKey) {
        return refreshTokenStore.get(refreshTokenKey);
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken refreshToken) {
        String refreshTokenKey = refreshToken.getValue();
        refreshTokenToAuthentication.remove(refreshTokenKey);
        String accessTokenKey = refreshTokenToAccessToken.remove(refreshTokenKey);
        accessTokenToRefreshToken.remove(accessTokenKey);
        refreshTokenStore.remove(refreshTokenKey);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        String refreshTokenKey = refreshToken.getValue();
        String accessTokenKey = refreshTokenToAccessToken.remove(refreshTokenKey);
        if (accessTokenKey != null) {
            removeAccessToken(accessTokenKey);
        }
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String username) {
        String approvalKey = getApprovalKey(clientId, username);
        Collection<OAuth2AccessToken> result = userNameToAccessToken.getTokens(approvalKey);
        return result != null ? Collections.<OAuth2AccessToken> unmodifiableCollection(result) :
                Collections.<OAuth2AccessToken> emptySet();
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        Collection<OAuth2AccessToken> result = clientIdToAccessToken.getTokens(clientId);
        return result != null ? Collections.<OAuth2AccessToken> unmodifiableCollection(result) :
                Collections.<OAuth2AccessToken> emptySet();
    }
}