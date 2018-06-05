package doitincloud.oauth2.services;

import doitincloud.oauth2.models.User;
import doitincloud.oauth2.repositories.UserRepo;

import doitincloud.oauth2.supports.OAuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreAuthUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreAuthUserDetailsService.class);

    private TokenStore tokenStore;

    private UserRepo userRepo;

    public PreAuthUserDetailsService(TokenStore tokenStore, UserRepo userRepo) {
        this.tokenStore = tokenStore;
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        String username = null;
        Object object = token.getPrincipal();
        if (object instanceof Authentication) {
            Authentication authentication = (Authentication) object;
            username = authentication.getName();
        } else if (object instanceof String) {
            username = (String) object;
        } else {
            username = token.getName();
        }
        if (username == null) {
            throw new UsernameNotFoundException("unable to figure out username");
        }
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("user " + username + " not found");
        }
        UserDetails userDetails = OAuthUtils.toUserDetails(user);
        //System.out.println("PreAuthUserDetailsService -> user:\n"+Utils.toPrettyJson(userDetails));
        return userDetails;
    }
}
