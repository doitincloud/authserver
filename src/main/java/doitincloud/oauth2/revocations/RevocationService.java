package doitincloud.oauth2.revocations;

public interface RevocationService {

    public void revoke(String value);

    boolean supports(String type);
}
