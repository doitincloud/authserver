package com.doitincloud.oauth2.supports;

import com.doitincloud.commons.Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AuthKeyGenerator {

    public static String extractKey(OAuth2Authentication authentication) {
        Map<String, Object> map = new LinkedHashMap();
        OAuth2Request authorizationRequest = authentication.getOAuth2Request();
        if (!authentication.isClientOnly()) {
            map.put("username", authentication.getName());
        }
        map.put("client_id", authorizationRequest.getClientId());
        if (authorizationRequest.getScope() != null) {
            Set<String> scopes = authorizationRequest.getScope();
            if (scopes != null && scopes.size() > 0) {
                map.put("scope", scopes);
            }
        }
        if (authorizationRequest.getRedirectUri() != null) {
            String redirecttUri = authorizationRequest.getRedirectUri();
            map.put("redirect_uri", redirecttUri);
        }
        return generateKey(map);
    }

    private static String generateKey(Map<String, Object> map) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(Utils.toJson(map).getBytes());
            String s = String.format("%032x", new BigInteger(1, bytes));
            return Utils.convertUuid(s);
        } catch (NoSuchAlgorithmException var4) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", var4);
        }
    }
}
