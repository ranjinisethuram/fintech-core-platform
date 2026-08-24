package com.fintech.security.service;

import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.security.properties.KeycloakProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakTokenService {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;

    private String accessToken;
    private Instant expiryTime;

    public KeycloakTokenService(KeycloakProperties keycloakProperties, RestTemplate restTemplate) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = restTemplate;
    }

    public String getToken(){
        if (accessToken == null || Instant.now().isAfter(expiryTime)) {
            synchronized (this) {
                if (accessToken == null || Instant.now().isAfter(expiryTime)) {
                    fetchNewToken();
                }
            }
        }
        return accessToken;
    }

    private void fetchNewToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("grant_type","client_credentials");
        requestMap.add("client_id",keycloakProperties.getClientId());
        requestMap.add("client_secret",keycloakProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestMap, headers);

        Map<String, Object> responseBody = new HashMap<>();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    keycloakProperties.getTokenUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            responseBody = response.getBody();
            if(responseBody != null) {
                accessToken = (String) responseBody.get("access_token");
                Integer expiresIn = (Integer) responseBody.get("expires_in");

                expiryTime = Instant.now().plusSeconds(expiresIn - 30);
            }
        } catch (Exception e) {
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
    }
}
