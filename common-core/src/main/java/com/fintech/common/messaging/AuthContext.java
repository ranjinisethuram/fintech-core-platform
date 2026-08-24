package com.fintech.common.messaging;

import java.util.List;

public class AuthContext {
    private String subject;
    private String clientId;
    private List<String> roles;
    private List<String> scopes;
    private boolean serviceToken;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public boolean isServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(boolean serviceToken) {
        this.serviceToken = serviceToken;
    }
}
