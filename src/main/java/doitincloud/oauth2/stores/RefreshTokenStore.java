package doitincloud.oauth2.stores;

import doitincloud.commons.Utils;
import doitincloud.oauth2.models.Token;
import doitincloud.oauth2.repositories.TokenRepo;
import doitincloud.oauth2.supports.RefreshTokenImpl;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

public class RefreshTokenStore {

    private TokenRepo tokenRepo;

    private String tokenType;

    public RefreshTokenStore(TokenRepo tokenRepo) {
        this.tokenRepo = tokenRepo;
        tokenType = OAuth2RefreshToken.class.getSimpleName();
    }

    public OAuth2RefreshToken get(String key) {
        Token token = tokenRepo.get(key, tokenType);
        if (token == null) {
            return null;
        }
        return Utils.toPojo(token.getData(), RefreshTokenImpl.class);
    }

    public String put(OAuth2RefreshToken refreshToken) {
        Token token = new Token(refreshToken.getValue(), tokenType, Utils.toMap(refreshToken));
        return tokenRepo.put(token);
    }

    public OAuth2RefreshToken remove(String key) {
        Token token = tokenRepo.remove(key, tokenType);
        if (token == null) {
            return null;
        }
        return Utils.toPojo(token.getData(), RefreshTokenImpl.class);
    }

    public boolean hasToken(String key) {
        return tokenRepo.hasToken(key, tokenType);
    }
    
}
