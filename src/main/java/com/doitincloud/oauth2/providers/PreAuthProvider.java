package com.doitincloud.oauth2.providers;

import com.doitincloud.oauth2.services.PreAuthUserDetailsService;
import com.doitincloud.oauth2.repositories.UserRepo;

import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import javax.annotation.PostConstruct;

public class PreAuthProvider extends PreAuthenticatedAuthenticationProvider {

    private PreAuthUserDetailsService userPreAuthService;

    public PreAuthProvider(TokenStore tokenStore, UserRepo userRepo){
        super();
        userPreAuthService = new PreAuthUserDetailsService(tokenStore, userRepo);
    }

    @PostConstruct
    public void init(){
        super.setPreAuthenticatedUserDetailsService(userPreAuthService);
    }

}
