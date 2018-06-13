package com.doitincloud.oauth2.stores;

import com.doitincloud.oauth2.models.Token;
import com.doitincloud.commons.Utils;
import com.doitincloud.oauth2.repositories.TokenRepo;
import com.doitincloud.oauth2.repositories.UserRepo;
import com.doitincloud.oauth2.supports.AuthKeyGenerator;
import com.doitincloud.oauth2.supports.OAuthUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public class AuthenticationStore {

    private UserRepo userRepo;

    private TokenRepo tokenRepo;

    private String tokenType;

    public AuthenticationStore(TokenRepo tokenRepo, UserRepo userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        tokenType = OAuth2Authentication.class.getSimpleName();
    }

    public OAuth2Authentication get(String key) {
        Token token = tokenRepo.get(key, tokenType);
        if (token == null) {
            return null;
        }
        return OAuthUtils.createOAuth2Authentication(token.getData(), userRepo);
    }

    public String put(OAuth2Authentication authentication) {
        String authKey = AuthKeyGenerator.extractKey(authentication);
        Token token = new Token(authKey, tokenType, Utils.toMap(authentication));
        return tokenRepo.put(token);
    }

    public OAuth2Authentication remove(String key) {
        Token token = tokenRepo.remove(key, tokenType);
        if (token == null) {
            return null;
        }
        return OAuthUtils.createOAuth2Authentication(token.getData(), userRepo);
    }

    public boolean hasToken(String key) {
        return tokenRepo.hasToken(key, tokenType);
    }
    
}
