package doitincloud.oauth2.revocations;

import doitincloud.oauth2.supports.AuthenticationContext;
import doitincloud.rdbcache.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenRevokeService extends AbstractRevocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenRevokeService.class);

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void revoke(String token) {

        OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
        if (accessToken == null) {
            LOGGER.trace("access_token not found: " + token);
            return;
        }

        AuthenticationContext context = new AuthenticationContext(clientDetailsService, userDetailsService);

        // the exactly same access token as authorized
        if (token.equals(context.getCurrentAccessToken())) {
            doRevoke(accessToken, null);
            return;
        }

        OAuth2Authentication auth = tokenStore.readAuthentication(accessToken);
        if (auth == null) {
            tokenStore.removeAccessToken(accessToken);
            return;
        }

        if (!revokeProcess(context, auth, accessToken, null)) {
            throw new InsufficientAuthenticationException("insufficient authority to perform revocation");
        }
    }

    public boolean supports(String type) {
        return "access_token".equals(type);
    }
}
