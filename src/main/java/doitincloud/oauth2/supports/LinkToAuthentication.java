package doitincloud.oauth2.supports;


import doitincloud.commons.Utils;
import doitincloud.oauth2.models.Token;
import doitincloud.oauth2.repositories.LinkRepo;
import doitincloud.oauth2.repositories.TokenRepo;
import doitincloud.oauth2.repositories.UserRepo;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.*;

public class LinkToAuthentication {

    private UserRepo userRepo;

    private TokenRepo tokenRepo;

    private LinkRepo linkRepo;

    private String type;

    private String linkedType;

    public LinkToAuthentication(TokenRepo tokenRepo, LinkRepo linkRepo, UserRepo userRepo, String type) {
        this.tokenRepo = tokenRepo;
        this.linkRepo = linkRepo;
        this.userRepo = userRepo;
        this.type = type;
        linkedType = OAuth2Authentication.class.getSimpleName();
    }

    public String getKey(String key) {
        List<Object> list = linkRepo.get(key, type);
        if (list == null) {
            return null;
        }
        return (String) list.get(0);
    }

    public String putKey(String key, String authKey) {
        linkRepo.put(key, type, authKey);
        return authKey;
    }

    public OAuth2Authentication get(String key) {
        String authKey = getKey(key);
        if (authKey == null) {
            return null;
        }
        Token token = tokenRepo.get(authKey, linkedType);
        if (token == null) {
            return null;
        }
        return OAuthUtils.createOAuth2Authentication(token.getData(), userRepo);
    }

    public String put(String key, OAuth2Authentication authentication) {
        String authKey = AuthKeyGenerator.extractKey(authentication);
        linkRepo.put(key, type, authKey);
        if (!tokenRepo.hasToken(authKey, linkedType)) {
            Token token = new Token(authKey, linkedType);
            token.setData(Utils.toMap(authentication));
            tokenRepo.put(token);
        }
        return authKey;
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