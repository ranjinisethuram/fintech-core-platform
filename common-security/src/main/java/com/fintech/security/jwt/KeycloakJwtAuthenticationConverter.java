package com.fintech.security.jwt;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakRealmRoleConverter roleConverter = new KeycloakRealmRoleConverter();

    @Override
    public @Nullable AbstractAuthenticationToken convert(Jwt sourceJwt) {
        return new JwtAuthenticationToken(
                sourceJwt,
                roleConverter.convert(sourceJwt)
        );
    }
}
