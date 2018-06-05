package doitincloud.oauth2.supports;


import doitincloud.commons.Utils;
import doitincloud.oauth2.models.Token;
import doitincloud.oauth2.repositories.LinkRepo;
import doitincloud.oauth2.repositories.TokenRepo;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import java.util.*;

public class LinkToRefreshToken {

    private TokenRepo tokenRepo;

    private LinkRepo linkRepo;

    private String type;

    private String linkedType;

    public LinkToRefreshToken(TokenRepo tokenRepo, LinkRepo linkRepo, String type) {
        this.tokenRepo = tokenRepo;
        this.linkRepo = linkRepo;
        this.type = type;
        linkedType = OAuth2RefreshToken.class.getSimpleName();
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
        linkRepo.put(key, type, tokenKeySet);
    }

    public OAuth2RefreshToken get(String key) {
        Set<String> tokenKeys = getKeys(key);
        if (tokenKeys == null) {
            return null;
        }
        String tokenKey = tokenKeys.iterator().next();
        Token token = tokenRepo.get(tokenKey, linkedType);
        if (token == null) {
            return null;
        }
        OAuth2RefreshToken refreshToken = Utils.toPojo(token.getData(), OAuth2RefreshToken.class);
        return refreshToken;
    }

    public String put(String key, OAuth2RefreshToken refreshToken) {
        String refreshTokenKey = refreshToken.getValue();
        linkRepo.put(key, type, refreshTokenKey);
        if (!tokenRepo.hasToken(refreshTokenKey, linkedType)) {
            Token token = new Token(refreshTokenKey, linkedType);
            token.setData(Utils.toMap(refreshToken));
            tokenRepo.put(token);
        }
        return refreshTokenKey;
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