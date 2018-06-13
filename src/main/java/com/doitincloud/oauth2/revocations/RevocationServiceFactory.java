package com.doitincloud.oauth2.revocations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RevocationServiceFactory {

    @Autowired
    private List<RevocationService> revocationServices;

    public RevocationService create(String type) {
        return revocationServices.stream()
                .filter(service -> service.supports(type))
                .findFirst().orElse(noopRevocationService());
    }

    private RevocationService noopRevocationService() {
        return new NoopRevocationService();
    }

}
