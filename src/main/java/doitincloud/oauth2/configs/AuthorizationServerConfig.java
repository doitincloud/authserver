package doitincloud.oauth2.configs;

import doitincloud.oauth2.repositories.ClientRepo;
import doitincloud.oauth2.repositories.LinkRepo;
import doitincloud.oauth2.repositories.TokenRepo;
import doitincloud.oauth2.repositories.UserRepo;
import doitincloud.oauth2.services.*;

import doitincloud.oauth2.stores.ApprovalStoreImpl;
import doitincloud.oauth2.stores.TokenStoreImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private TokenRepo tokenRepo;

    @Autowired
    private LinkRepo linkRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public ApprovalStore approvalStore() {
        return new ApprovalStoreImpl(tokenRepo);
    }

    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new AuthCodeServices(tokenRepo, linkRepo, userRepo);
    }

    @Bean
    public TokenStore tokenStore() {
        return new TokenStoreImpl(tokenRepo, linkRepo, userRepo);
    }

    @Bean
    public ClientDetailsService clientDetailsService() {
        return new ClientDetailsServiceImpl(clientRepo);
    }

    @Bean
    public TokenServices tokenServices() {
        TokenServices tokenServices = new TokenServices();
        tokenServices.setAuthenticationManager(authenticationManager);
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setReuseRefreshToken(false);
        return tokenServices;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .pathMapping("/oauth/token", "/oauth/v1/token")
                .pathMapping("/oauth/authorize", "/oauth/v1/authorize")
                .pathMapping("/oauth/check_token", "/oauth/v1/check_token")
                .pathMapping("/oauth/token_key", "/oauth/v1/token_key")
                .pathMapping("/oauth/confirm_access", "/oauth/v1/confirm_access")
                .pathMapping("/oauth/error", "/oauth/v1/error")
                .authenticationManager(authenticationManager)
                .tokenServices(tokenServices())
                .authorizationCodeServices(authorizationCodeServices())
                .approvalStore(approvalStore());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        //@formatter:off
        oauthServer
                .passwordEncoder(passwordEncoder)  // for client secret
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()") //allow check token
                .allowFormAuthenticationForClients();
        //@formatter:on
    }
}
