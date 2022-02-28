package com.entiros.starlify.connector.api.service;



import com.entiros.starlify.connector.api.dto.mashery.APIDefinitions;
import com.entiros.starlify.connector.api.dto.mashery.APIEndPointDetails;
import com.entiros.starlify.connector.api.dto.mashery.APIEndPoints;
import com.entiros.starlify.connector.api.dto.mashery.MasheryResponse;

import java.util.List;

public interface MashreyService {
    public MasheryResponse<APIDefinitions> getAPIDefinitions(String accessToken);

    public MasheryResponse<APIEndPoints> getApiEndpoints(String accessToken, String apiDefinitionId);

    public APIEndPointDetails getEndpointDetails(String accessToken, String apiDefinitionId, String endpointId);
}
