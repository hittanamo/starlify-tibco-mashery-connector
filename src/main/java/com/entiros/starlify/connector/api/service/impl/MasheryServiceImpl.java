package com.entiros.starlify.connector.api.service.impl;

import com.entiros.starlify.connector.api.dto.mashery.APIDefinitions;
import com.entiros.starlify.connector.api.dto.mashery.APIEndPointDetails;
import com.entiros.starlify.connector.api.dto.mashery.APIEndPoints;
import com.entiros.starlify.connector.api.dto.mashery.MasheryResponse;
import com.entiros.starlify.connector.api.service.MashreyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MasheryServiceImpl implements MashreyService {

    @Autowired
    @Qualifier("masheryRestTemplate")
    private RestTemplate restTemplate;

    @Value("${mashrey.server.url}")
    private String apiServer;


    @Override
    public MasheryResponse<APIDefinitions> getAPIDefinitions(String accessToken) {
        HttpHeaders headers = getHttpHeaders(accessToken);
        ResponseEntity<MasheryResponse<APIDefinitions>> response = restTemplate.exchange(apiServer + "/api/cc/api-definitions?limit=10&offset=0",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<MasheryResponse<APIDefinitions>>() {
                });

        return response.getBody();
    }

    private HttpHeaders getHttpHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + accessToken);
        return headers;
    }

    @Override
    public MasheryResponse<APIEndPoints> getApiEndpoints(String accessToken, String apiDefinitionId) {
        HttpHeaders headers = getHttpHeaders(accessToken);
        ResponseEntity<MasheryResponse<APIEndPoints>> response = restTemplate.exchange(apiServer + "/api/cc/api-definitions/{apiDefinitionId}/api-endpoints?limit=1000&offset=0",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<MasheryResponse<APIEndPoints>>() {
                }, apiDefinitionId);
        return response.getBody();
    }

    @Override
    public APIEndPointDetails getEndpointDetails(String accessToken, String apiDefinitionId, String endpointId) {
        HttpHeaders headers = getHttpHeaders(accessToken);
        ResponseEntity<APIEndPointDetails> response = restTemplate.exchange(apiServer + "/api/cc/api-definitions/{apiDefinitionId}/api-endpoints/{endpointId}/detection",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<APIEndPointDetails>() {
                }, apiDefinitionId, endpointId);
        return response.getBody();
    }
}
