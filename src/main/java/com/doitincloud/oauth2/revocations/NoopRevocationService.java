package com.doitincloud.oauth2.revocations;

public class NoopRevocationService implements RevocationService {

    @Override
    public void revoke(String value) { }

    @Override
    public boolean supports(String type) { return false; }

}
