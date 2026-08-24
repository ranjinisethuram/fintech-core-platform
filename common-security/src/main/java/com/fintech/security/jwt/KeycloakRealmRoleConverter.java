package com.fintech.security.jwt;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String CLIENT_NAME = "fintech-client";
    private static final String ROLES = "roles";
    private static final String SCOPE = "scope";

    @Override
    public @Nullable Collection<GrantedAuthority> convert(Jwt jwtSource) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        Map<String, Object> resourceAccess = jwtSource.getClaim(RESOURCE_ACCESS);

        if(resourceAccess != null && resourceAccess.containsKey(CLIENT_NAME)){
            Map<String, Object> client = (Map<String, Object>) resourceAccess.get(CLIENT_NAME);
            List<String> roles = (List<String>)client.get(ROLES);
            if(roles != null){
                roles.forEach(role ->
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role))
                );
            }

            String scope = jwtSource.getClaimAsString(SCOPE);
            if (scope != null) {
                Arrays.stream(scope.split(" "))
                        .forEach(s ->
                                grantedAuthorities.add(new SimpleGrantedAuthority("SCOPE_" + s))
                        );
            }
        }
        /*grantedAuthorities.forEach(a ->
                System.out.println("CONVERTER AUTH: " + a.getAuthority())
        );*/
        return grantedAuthorities;
    }
}
