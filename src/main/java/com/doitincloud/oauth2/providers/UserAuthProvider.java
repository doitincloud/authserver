package com.doitincloud.oauth2.providers;

import com.doitincloud.commons.Utils2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserAuthProvider implements AuthenticationProvider {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {

        final String username = auth.getName();
        final String password = auth.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        PasswordEncoder encoder = Utils2.passwordEncoder();
        if (!encoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Authentication failed for user = " + username);
        }
        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}
