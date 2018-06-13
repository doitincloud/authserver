package com.doitincloud.oauth2.repositories;

import com.doitincloud.oauth2.models.Client;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepo {

    Client findByClientId(String clientId);

    void delete(String clientId);
}
