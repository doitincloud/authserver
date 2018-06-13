package com.doitincloud.oauth2.services;

import com.doitincloud.oauth2.repositories.LinkRepo;
import com.doitincloud.oauth2.repositories.TokenRepo;
import com.doitincloud.oauth2.repositories.UserRepo;
import com.doitincloud.oauth2.supports.LinkToAuthentication;

import com.doitincloud.commons.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;

public class AuthCodeServices implements AuthorizationCodeServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthCodeServices.class);

    private LinkToAuthentication AuthorizationCodeToAuthentication;

    public AuthCodeServices(TokenRepo tokenRepo, LinkRepo linkRepo, UserRepo userRepo) {
        AuthorizationCodeToAuthentication =
                new LinkToAuthentication(tokenRepo, linkRepo, userRepo,"AuthorizationCode/OAuth2Authentication");
    }

    @Override
    public String createAuthorizationCode(OAuth2Authentication authentication) {

        String code = Utils.generateUuid();
        AuthorizationCodeToAuthentication.put(code, authentication);
        return code;
    }

    @Override
    public OAuth2Authentication consumeAuthorizationCode(String code) throws InvalidGrantException {
        OAuth2Authentication authentication = AuthorizationCodeToAuthentication.get(code);
        if (authentication == null) {
            throw new InvalidGrantException("no authentication found");
        }
        return authentication;
    }
}
