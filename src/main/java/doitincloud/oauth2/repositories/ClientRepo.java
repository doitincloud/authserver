package doitincloud.oauth2.repositories;

import doitincloud.oauth2.models.Client;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepo {

    Client findByClientId(String clientId);

    void delete(String clientId);
}
