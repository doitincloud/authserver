package doitincloud.oauth2.repositories;

import doitincloud.oauth2.models.Token;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepo {

    public Token get(String key, String type);

    public String put(Token token);

    public Token remove(String key, String type);

    public boolean hasToken(String key, String type);
}
