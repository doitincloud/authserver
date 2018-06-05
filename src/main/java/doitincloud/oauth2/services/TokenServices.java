package doitincloud.oauth2.services;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.*;

public class TokenServices extends DefaultTokenServices {

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        OAuth2Authentication authentication = super.loadAuthentication(accessToken);
        //System.out.println("OAuth2Authentication:\n" + Utils.toPrettyJson(authentication));
        return authentication;
    }
}
