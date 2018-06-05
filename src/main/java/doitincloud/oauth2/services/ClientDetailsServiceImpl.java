package doitincloud.oauth2.services;

import doitincloud.commons.Utils;
import doitincloud.oauth2.supports.OAuthUtils;
import doitincloud.oauth2.models.Client;
import doitincloud.oauth2.repositories.ClientRepo;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class ClientDetailsServiceImpl implements ClientDetailsService {

    private ClientRepo clientRepo;

    public ClientDetailsServiceImpl(ClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {

        Client client = clientRepo.findByClientId(clientId);
        if (client == null) {
            throw new ClientRegistrationException("client " + clientId + " not found");
        }
        ClientDetails clientDetails = OAuthUtils.toClientDetails(client);
        //System.out.println("client:\n"+Utils.toPrettyJson(clientDetails));
        return clientDetails;
    }
}
