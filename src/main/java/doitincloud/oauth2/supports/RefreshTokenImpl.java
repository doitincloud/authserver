package doitincloud.oauth2.supports;

import org.springframework.security.oauth2.common.OAuth2RefreshToken;

public class RefreshTokenImpl implements OAuth2RefreshToken {

    private String value;

    public RefreshTokenImpl() {
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
