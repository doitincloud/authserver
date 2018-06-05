package doitincloud.oauth2.revocations;

import doitincloud.oauth2.supports.AuthenticationContext;
import doitincloud.rdbcache.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RevokeByUsernameService extends AbstractRevocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeByUsernameService.class);

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void revoke(String username) {

        AuthenticationContext context = new AuthenticationContext(clientDetailsService, userDetailsService);
        String clientId = context.getCurrentClientId();
        Collection<OAuth2AccessToken> accessTokens = tokenStore.findTokensByClientIdAndUserName(clientId, username);
        if (accessTokens == null || accessTokens.size() == 0) {
            LOGGER.trace("access_token not found for user " + username + " with client " + clientId);
            return;
        }

        for (OAuth2AccessToken accessToken : accessTokens) {

            OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
            OAuth2Authentication auth = tokenStore.readAuthentication(accessToken);
            if (auth == null) {
                tokenStore.removeAccessToken(accessToken);
                if (accessToken != null) {
                    tokenStore.removeAccessToken(accessToken);
                }
                if (refreshToken != null) {
                    tokenStore.removeRefreshToken(refreshToken);
                }
                continue;
            }

            if (!revokeProcess(context, auth, accessToken, refreshToken)) {
                throw new InsufficientAuthenticationException("insufficient authority to perform revocation");
            }
        }
    }

    public boolean supports(String type) {
        return "username".equals(type);
    }

}
