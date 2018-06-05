package doitincloud.oauth2.controllers;

import doitincloud.oauth2.revocations.RevocationService;
import doitincloud.oauth2.revocations.RevocationServiceFactory;
import doitincloud.oauth2.supports.MissingParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.MissingFormatArgumentException;

@RestController
public class TokenRevokeController {

    @Autowired
    private RevocationServiceFactory revocationServiceFactory;

    @PostMapping("/oauth/v1/revoke")
    public ResponseEntity<String> revoke(@RequestParam Map<String, String> params)
        throws MissingParameterException {
        String type = params.get("type");
        if (type == null) {
            throw new MissingParameterException("missing required parameter type");
        }
        String value = params.get("value");
        if (value == null) {
            throw new MissingParameterException("missing required parameter value");
        }
        RevocationService revocationService = revocationServiceFactory.create(type);
        revocationService.revoke(value);
        return ResponseEntity.ok().build();
    }
}
