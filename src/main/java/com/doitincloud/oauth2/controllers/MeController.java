package com.doitincloud.oauth2.controllers;

import com.doitincloud.oauth2.supports.OAuthUtils;
import com.doitincloud.commons.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class MeController {

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping({ "/v1/me" })
    public Map<String, Object> me(HttpServletRequest request) {

        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
                .getContext().getAuthentication();
        String grantType = authentication.getOAuth2Request().getGrantType();

        ClientDetails clientDetails = null;

        boolean trusted = false;

        if ("password".equals(grantType)) {
            trusted = true;
        } else {
            String clientId = authentication.getOAuth2Request().getClientId();
            clientDetails = clientDetailsService.loadClientByClientId(clientId);
            Set<String> authorities = OAuthUtils.getAuthorities(clientDetails.getAuthorities());
            if (authorities.contains("ROLE_TRUSTED") || authorities.contains("ROLE_INTERNAL")) {
                if (authentication.isClientOnly()) {
                    trusted = true;
                } else if (request.isUserInRole("ADMIN") || request.isUserInRole("SUPER") || request.isUserInRole("ROOT")) {
                    trusted = true;
                }
            }
        }

        String name = authentication.getName();
        Map<String, Object> map = null;

        if (trusted) {
            if (authentication.isClientOnly()) {
                if (clientDetails == null) {
                    clientDetails = clientDetailsService.loadClientByClientId(name);
                }
                map = Utils.toMap(clientDetails);
                map.remove("clientSecret");
            } else {
                map = Utils.toMap(userDetailsService.loadUserByUsername(name));
                map.remove("password");
            }
        } else {
            map = new LinkedHashMap<>();
        }

        map.put("name", name);

        return map;
    }
}
