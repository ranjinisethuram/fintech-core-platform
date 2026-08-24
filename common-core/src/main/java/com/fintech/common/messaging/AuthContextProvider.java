package com.fintech.common.messaging;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class AuthContextProvider {

    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String CLIENT_NAME = "fintech-client";
    private static final String ROLES = "roles";
    private static final String SCOPE = "scope";

    public AuthContext getSecurityAuthContext(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return fromJwt(jwt);
        }

        //fallback for scheduled / internal flows
        return systemAuthContext();
    }

    private AuthContext fromJwt(Jwt jwt) {
        AuthContext authContext = new AuthContext();
        authContext.setSubject(jwt.getSubject());
        authContext.setClientId(jwt.getClaim("client_id"));
        authContext.setServiceToken(jwt.getClaim("client_id") != null);
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS);
        List<String> roles = new ArrayList<>();
        List<String> scopes = new ArrayList();
        if(resourceAccess != null && resourceAccess.containsKey(CLIENT_NAME)) {
            Map<String, Object> client = (Map<String, Object>) resourceAccess.get(CLIENT_NAME);
            roles = (List<String>) client.get(ROLES);
        }
        authContext.setRoles(roles);
        String scope = jwt.getClaimAsString(SCOPE);
        if (scope != null) {
            scopes = Arrays.stream(scope.split(" ")).toList();
        }
        authContext.setScopes(scopes);
        return authContext;
    }

    private AuthContext systemAuthContext() {
        AuthContext authContext = new AuthContext();
        authContext.setServiceToken(true);
        authContext.setClientId("orchestration-service");
        authContext.setRoles(List.of("SERVICE"));
        authContext.setScopes(List.of("internal:execute"));
        return authContext;
    }
}
