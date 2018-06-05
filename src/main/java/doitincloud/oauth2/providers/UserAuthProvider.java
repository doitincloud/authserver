package doitincloud.oauth2.providers;

import doitincloud.commons.Utils;
import doitincloud.oauth2.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

public class UserAuthProvider implements AuthenticationProvider {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {

        final String username = auth.getName();
        final String password = auth.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        PasswordEncoder encoder = Utils.passwordEncoder();
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
