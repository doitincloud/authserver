package com.doitincloud.oauth2.supports;


import com.doitincloud.oauth2.models.Token;
import com.doitincloud.commons.Utils;
import com.doitincloud.oauth2.repositories.LinkRepo;
import com.doitincloud.oauth2.repositories.TokenRepo;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.*;

public class LinkToAccessToken {

    private TokenRepo tokenRepo;

    private LinkRepo linkRepo;

    private String type;

    private String linkedType;

    private boolean collection = false;

    public LinkToAccessToken(TokenRepo tokenRepo, LinkRepo linkRepo, String type) {
        this.tokenRepo = tokenRepo;
        this.linkRepo = linkRepo;
        this.type = type;
        linkedType = OAuth2AccessToken.class.getSimpleName();
    }

    public LinkToAccessToken(TokenRepo tokenRepo, LinkRepo linkRepo, String type, boolean collection) {
        this.tokenRepo = tokenRepo;
        this.linkRepo = linkRepo;
        this.type = type;
        this.collection = collection;
        linkedType = OAuth2AccessToken.class.getSimpleName();
    }

    public Set<String> getKeys(String key) {
        List<Object> list = linkRepo.get(key, type);
        if (list == null) {
            return null;
        }
        Set<String> set = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            set.add((String) list.get(i));
        }
        return set;
    }

    public void putKeys(String key, Set<String> tokenKeySet) {
        if (collection) {
            linkRepo.add(key, type, tokenKeySet);
        } else {
            linkRepo.put(key, type, tokenKeySet);
        }
    }

    public Set<OAuth2AccessToken> getTokens(String key) {
        Set<String> tokenKeys = getKeys(key);
        if (tokenKeys == null) {
            return null;
        }
        Set<OAuth2AccessToken> set = new HashSet<>();
        for (String tokenKey: tokenKeys) {
            Token token = tokenRepo.get(tokenKey, linkedType);
            if (token == null) {
                continue;
            }
            OAuth2AccessToken accessToken = Utils.toPojo(token.getData(), OAuth2AccessToken.class);
            set.add(accessToken);
        }
        return set;
    }

    public void removeToken(String key, OAuth2AccessToken token) {
        linkRepo.remove(key, token.getValue());
    }

    public OAuth2AccessToken get(String key) {
        Set<String> tokenKeys = getKeys(key);
        if (tokenKeys == null) {
            return null;
        }
        String tokenKey = tokenKeys.iterator().next();
        Token token = tokenRepo.get(tokenKey, linkedType);
        if (token == null) {
            return null;
        }
        OAuth2AccessToken accessToken = Utils.toPojo(token.getData(), OAuth2AccessToken.class);
        return accessToken;
    }

    public String put(String key, OAuth2AccessToken accessToken) {
        String accessTokenKey = accessToken.getValue();
        if (collection) {
            linkRepo.add(key, type, accessTokenKey);
        } else {
            linkRepo.put(key, type, accessTokenKey);
        }
        if (!tokenRepo.hasToken(accessTokenKey, linkedType)) {
            Token token = new Token(accessTokenKey, linkedType);
            token.setData(Utils.toMap(accessToken));
            tokenRepo.put(token);
        }
        return accessTokenKey;
    }

    public String remove(String key) {
        List<Object> list = linkRepo.get(key, type);
        if (list == null) {
            return null;
        }
        linkRepo.remove(key, type);
        return (String) list.get(0);
    }

    public boolean hasLink(String key) {
        return linkRepo.hasLink(key, type);
    }
    
}