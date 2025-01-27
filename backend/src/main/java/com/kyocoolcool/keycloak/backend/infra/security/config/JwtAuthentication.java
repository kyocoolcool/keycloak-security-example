package com.kyocoolcool.keycloak.backend.infra.security.config;

import com.kyocoolcool.keycloak.backend.infra.security.AccessToken;
import lombok.ToString;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@ToString
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final AccessToken accessToken;

    public JwtAuthentication(AccessToken accessToken) {
        super(accessToken.getAuthorities());
        this.accessToken = accessToken;
    }

    @Override
    public Object getCredentials() {
        return accessToken.getValueAsString();
    }

    @Override
    public Object getPrincipal() {
        return accessToken.getUsername();
    }
}
