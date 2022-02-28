package com.entiros.starlify.connector.api.dto.mashery;

import lombok.Data;

import java.util.List;

@Data
public class APIDefinitions extends MasheryBaseDto {
    private String identifier;
    private String name;
    private String description;
    private String version;
    private int qps_limit_overall;
    private String cross_domain_policy;
    private String robots_policy;
    private String rfc_3986_encoded;
    private List<String> roles;
}
