package doitincloud.oauth2.revocations;

import doitincloud.oauth2.supports.AuthenticationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Set;

public abstract class AbstractRevocationService implements RevocationService {

    @Autowired
    protected TokenStore tokenStore;


    public boolean revokeProcess(AuthenticationContext context, OAuth2Authentication auth,
                                 OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {

        String targetUsername = context.getUsername(auth);
        String targetClientId = context.getClientId(auth);

        // always allow self token revocation
        //
        if (context.getCurrentAuth().isClientOnly() && auth.isClientOnly()) {
            if (targetClientId != null && targetClientId.equals(context.getCurrentClientId())) {
                return doRevoke(accessToken, refreshToken);
            } else {
                return false;
            }
        } else if (targetUsername != null && targetUsername.equals(context.getCurrentUsername())) {
            return doRevoke(accessToken, refreshToken);
        }

        // root and super user has the most power
        //
        if (context.getCurrentUserAuhorities().contains("ROLE_ROOT") ||
            context.getCurrentUserAuhorities().contains("ROLE_SUPER")) {
            return doRevoke(accessToken, refreshToken);
        }

        if (!context.getCurrentUserAuhorities().contains("ROLE_ADMIN")) {
            return false;
        }

        if (context.getCurrentUserAuhorities() == null) {
            return false;
        }

        // created by by same trusted client, admin allows
        //
        if (targetClientId != null &&
            targetClientId.equals(context.getCurrentClientId())) {
            return doRevoke(accessToken, refreshToken);
        }

        // otherwise by internal client, admin allow
        //
        if (context.getCurrentClientAuhorities().contains("ROLE_INTERNAL")) {
            return doRevoke(accessToken, refreshToken);
        }

        return false;
    }

    protected boolean doRevoke(OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        boolean ok = false;
        if (accessToken != null) {
            ok = true;
            tokenStore.removeAccessToken(accessToken);
        }
        if (refreshToken != null) {
            ok = true;
            tokenStore.removeRefreshToken(refreshToken);
        }
        return ok;
   }
}
