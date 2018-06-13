package com.doitincloud.oauth2.stores;

import com.doitincloud.oauth2.models.Token;
import com.doitincloud.commons.Utils;
import com.doitincloud.oauth2.repositories.TokenRepo;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class AccessTokenStore {

    private TokenRepo tokenRepo;

    private String tokenType;

    public AccessTokenStore(TokenRepo tokenRepo) {
        this.tokenRepo = tokenRepo;
        tokenType = OAuth2AccessToken.class.getSimpleName();
    }

    public OAuth2AccessToken get(String key) {
        Token token = tokenRepo.get(key, tokenType);
        if (token == null) {
            return null;
        }
        return Utils.toPojo(token.getData(), OAuth2AccessToken.class);
    }

    public String put(OAuth2AccessToken accessToken) {
        Token token = new Token(accessToken.getValue(), tokenType, Utils.toMap(accessToken));
        return tokenRepo.put(token);
    }

    public OAuth2AccessToken remove(String key) {
        Token token = tokenRepo.remove(key, tokenType);
        if (token == null) {
            return null;
        }
        return Utils.toPojo(token.getData(), OAuth2AccessToken.class);
    }

    public boolean hasToken(String key) {
        return tokenRepo.hasToken(key, tokenType);
    }

}
