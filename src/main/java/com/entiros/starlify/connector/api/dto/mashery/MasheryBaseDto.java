package com.entiros.starlify.connector.api.dto.mashery;

import lombok.Data;

@Data
public class MasheryBaseDto {
    private String id;
    private String created;
    private String updated;
    private String swagger_json;
    private String swagger_uri;
    private String api_domain_id;
    private String org_id;
    private String org_name;
}
