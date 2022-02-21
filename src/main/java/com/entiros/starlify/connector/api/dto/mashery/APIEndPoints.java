package com.entiros.starlify.connector.api.dto.mashery;

import lombok.Data;

@Data
public class APIEndPoints extends MasheryBaseDto {
    private String api_definition_id;
    private String name;
    private String type;
    private String public_endpoint_address_protocol;
    private String require_mtls;
    private String public_endpoint_address_path;
    private String tm_endpoint_address_protocol;
    private String public_traffic_manager_domain;
    private String tm_endpoint_address_domain;
    private String tm_endpoint_address_path;
    private String tm_endpoint_address_params;
    private String api_https_client_profile_id;
    private String user_controlled_error_location_key;
    private String user_controlled_error_location;
    private String error_set_id;
    private String error_set_name;
}
