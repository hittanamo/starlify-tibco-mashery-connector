package com.entiros.starlify.connector.api.dto.mashery;

import lombok.Data;

import java.util.List;

@Data
public class APIEndPointDetails extends MasheryBaseDto {
    private String api_definition_id;
    private String custom_authentication_adapter;
    private boolean delete;
    private boolean get;
    private boolean grant_type_authorization_code;
    private boolean grant_type_client_credentials;
    private boolean grant_type_implicit;
    private boolean grant_type_password;
    private boolean head;
    private String key_field_identifier;
    private List<String> method_location;
    private String method_location_identifier;
    private boolean options;
    private boolean patch;
    private boolean post;
    private boolean put;
    private String request_authentication_type;
}
